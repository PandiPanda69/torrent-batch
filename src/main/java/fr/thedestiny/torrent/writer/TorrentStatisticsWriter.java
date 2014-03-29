package fr.thedestiny.torrent.writer;

import java.util.List;

import lombok.Setter;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.transform.Transformers;
import org.springframework.batch.item.ItemWriter;

import fr.thedestiny.torrent.bean.TorrentBean;
import fr.thedestiny.torrent.util.transmission.mapping.TorrentListingResponse.Torrent;

/**
 * Writer permettant de persister en base de donn�es les statistics 
 * des torrents {@link Torrent} en entr�e.
 * @author S�bastien
 */
public class TorrentStatisticsWriter implements ItemWriter<Torrent> {	
	
	@Setter
	private SessionFactory sessionFactory;
	
	/**
	 * Constructeur
	 * @param sf Session factory
	 */
	public TorrentStatisticsWriter(SessionFactory sf) {
		sessionFactory = sf;
	}
	
	@Override
	public void write(List<? extends Torrent> in) throws Exception {		
		
		// Open new session and begin transaction
		Session session = sessionFactory.openSession();		
		Transaction tx = session.beginTransaction();
		
		try {
		
			/*
			 * Pour chacun des torrents � traiter
			 * 	- On v�rifie s'il est d�j� persist�
			 * 		- Si non on le persiste
			 * 		- Si oui on v�rifie que celui en base et celui remont� par RPC
			 * 			sont identiques (taille et statut) et on met � jour la base
			 * 			le cas �ch�ant.
			 * 	- On ins�re ses statistiques
			 * 	
			 */
			for(Torrent current : in) {
				
				TorrentBean torrent = getTorrentFromDatabase(current.getHashString(), session);
				if( torrent == null) {				
					insertTorrentInDatabase(current, session);
					torrent = getTorrentFromDatabase(current.getHashString(), session);
					if(torrent == null) {
						throw new NullPointerException("Cannot insert torrent.");
					}
				}
				else if(hasTorrentChanged(current, torrent)) {
					updateTorrent(current, torrent, session);
				}
				
				insertTorrentStatistics(torrent.getId(), current.getUploadedEver(), session);				
			}
		
			tx.commit();		
		}
		catch(Exception ex) {
			ex.printStackTrace();
			tx.rollback();
		}
		finally {
			// Do not forget to close session
			session.close();
		}		
	}
	
	/**
	 * 
	 * @param hash HashString du torrent � rechercher
	 * @param session La session courante
	 * @return Instance du bean mapp� � la BDD
	 */
	private TorrentBean getTorrentFromDatabase(String hash, Session session) {
		
		Query query = session.createSQLQuery("SELECT id, name, hash, downloadedBytes, status, trackerError FROM Torrent WHERE hash like :hash").setParameter("hash", hash);
		TorrentBean torrent = (TorrentBean) query.setResultTransformer(Transformers.aliasToBean(TorrentBean.class)).uniqueResult();
		
		return torrent;
	}
	
	/**
	 * Persiste un nouveau torrent en base de donn�es.
	 * @param torrent Le torrent r�cup�r� via un appel RPC � Transmission
	 * @param session La session courante
	 */
	private void insertTorrentInDatabase(Torrent torrent, Session session) {
		
		Query query = session.createSQLQuery("INSERT INTO Torrent(name, hash, downloadedBytes, status, trackerError) VALUES(:name, :hash, :downloadedBytes, 'ACTIVE', :trackerError)")
				.setParameter("name", torrent.getName())
				.setParameter("hash", torrent.getHashString())
				.setParameter("downloadedBytes", torrent.getSize())
				.setParameter("trackerError", torrent.getErrorString());
		
		query.executeUpdate();		
	}
	
	/**
	 * Ins�re les statistiques d'un torrent en base de donn�es
	 * @param torrentId Identifiant du torrent de r�f�rence persist� en base
	 * @param uploadedBytes Quantit� d'octets partag�s � ce jour
	 * @param session La session courante
	 */
	private void insertTorrentStatistics(Integer torrentId, Long uploadedBytes, Session session) {
		
		Query query = session.createSQLQuery("INSERT INTO TorrentStat(id_torrent, uploadedBytes) VALUES(:id, :uploadedBytes)")
				.setParameter("id", torrentId)
				.setParameter("uploadedBytes", uploadedBytes);
		
		query.executeUpdate();
	}
	
	/**
	 * Détermine si le torrent en base est différent aux informations remontées par l'appel
	 * RPC à Transmission
	 * @param current Le torrent récupéré via un appel RPC à Transmission
	 * @param bean Le torrent actuellement en base
	 * @return Vrai si le torrent est différent, faux le cas contraire
	 */
	private boolean hasTorrentChanged(Torrent current, TorrentBean bean) {
		
		return (
				!bean.getStatus().equals("ACTIVE")) || 
				(!current.getSize().equals(Long.valueOf(bean.getDownloadedBytes())) || 
				(!current.getErrorString().equals(bean.getTrackerError()))
		);
	}
	
	/**
	 * Met à jour le torrent au cas où celui en base contient des informations obsolètes.
	 * @param current Le torrent récupéré via un appel RPC à Transmission
	 * @param bean Le torrent actuellement en base
	 * @param session La session en cours
	 */
	private void updateTorrent(Torrent current, TorrentBean bean, Session session) {
		
		Query query = session.createSQLQuery("UPDATE Torrent SET status = 'ACTIVE', downloadedBytes = :downloadedBytes, trackerError = :trackerError WHERE id = :id")
				.setParameter("downloadedBytes", current.getSize())
				.setParameter("id", bean.getId())
				.setParameter("trackerError", current.getErrorString());
		
		query.executeUpdate();
	}
}
