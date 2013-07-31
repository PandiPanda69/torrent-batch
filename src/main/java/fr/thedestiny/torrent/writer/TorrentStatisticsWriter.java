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
 * Writer permettant de persister en base de données les statistics 
 * des torrents {@link Torrent} en entrée.
 * @author Sébastien
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
			 * Pour chacun des torrents à traiter
			 * 	- On vérifie s'il est déjà persisté
			 * 		- Si non on le persiste
			 * 		- Si oui on vérifie que celui en base et celui remonté par RPC
			 * 			sont identiques (taille et statut) et on met à jour la base
			 * 			le cas échéant.
			 * 	- On insère ses statistiques
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
	 * @param hash HashString du torrent à rechercher
	 * @param session La session courante
	 * @return Instance du bean mappé à la BDD
	 */
	private TorrentBean getTorrentFromDatabase(String hash, Session session) {
		
		Query query = session.createSQLQuery("SELECT id, name, hash, downloadedBytes, status FROM Torrent WHERE hash like :hash").setParameter("hash", hash);
		TorrentBean torrent = (TorrentBean) query.setResultTransformer(Transformers.aliasToBean(TorrentBean.class)).uniqueResult();
		
		return torrent;
	}
	
	/**
	 * Persiste un nouveau torrent en base de données.
	 * @param torrent Le torrent récupéré via un appel RPC à Transmission
	 * @param session La session courante
	 */
	private void insertTorrentInDatabase(Torrent torrent, Session session) {
		
		Query query = session.createSQLQuery("INSERT INTO Torrent(name, hash, downloadedBytes, status) VALUES(:name, :hash, :downloadedBytes, 'ACTIVE')")
				.setParameter("name", torrent.getName())
				.setParameter("hash", torrent.getHashString())
				.setParameter("downloadedBytes", torrent.getSize());
		
		query.executeUpdate();		
	}
	
	/**
	 * Insère les statistiques d'un torrent en base de données
	 * @param torrentId Identifiant du torrent de référence persisté en base
	 * @param uploadedBytes Quantité d'octets partagés à ce jour
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
		
		return (!bean.getStatus().equals("ACTIVE")) || (!current.getSize().equals(Long.valueOf(bean.getDownloadedBytes())));
	}
	
	/**
	 * Met à jour le torrent au cas où celui en base contient des informations obsolètes.
	 * @param current Le torrent récupéré via un appel RPC à Transmission
	 * @param bean Le torrent actuellement en base
	 * @param session La session en cours
	 */
	private void updateTorrent(Torrent current, TorrentBean bean, Session session) {
		
		Query query = session.createSQLQuery("UPDATE Torrent SET status = 'ACTIVE', downloadedBytes = :downloadedBytes WHERE id = :id")
				.setParameter("downloadedBytes", current.getSize())
				.setParameter("id", bean.getId());
		
		query.executeUpdate();
	}
}
