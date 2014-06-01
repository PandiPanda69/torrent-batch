package fr.thedestiny.torrent.writer;

import java.io.File;
import java.util.List;
import java.util.Map;

import lombok.Setter;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.batch.item.ItemWriter;

import fr.thedestiny.bencod.io.BencodFileInputStream;
import fr.thedestiny.bencod.parser.BencodFileFormatException;
import fr.thedestiny.bencod.parser.BencodParser;

public class TorrentQueueWriter implements ItemWriter<String> {

	private static final Logger LOGGER = Logger.getLogger(TorrentQueueWriter.class);
	
	@Setter
	private SessionFactory sessionFactory;
	
	@Setter
	private String queuePath;

	@SuppressWarnings("unchecked")
	@Override
	public void write(List<? extends String> items) throws Exception {

		for(String current : items) {

			String fullPath = new StringBuilder(queuePath).append(File.separatorChar).append(current).toString();
			BencodFileInputStream bfis = null;
			
			try {
				bfis = new BencodFileInputStream(fullPath);
				BencodParser parser = new BencodParser(bfis);
			
				Map<String, Object> result = (Map<String, Object>) parser.parse();
				Map<String, Object> info   = (Map<String, Object>) result.get("info");
			
				String name = info.get("name").toString();
				int size = 0;
				
				List<Map<String, Object>> torrentFiles = (List<Map<String, Object>>) info.get("files");
				if(torrentFiles != null) {				
					for(Map<String, Object> torrentFile : torrentFiles) {
						size += (Integer) torrentFile.get("length");
					}
				}
				else {
					size = (Integer) info.get("length");
				}

				insertTorrentInQueue(name, size, current);
			}
			catch(BencodFileFormatException ex) {
				LOGGER.error("File " + current + " cannot be read.", ex);
			}
			catch(Exception ex) {
				LOGGER.error("Unexpected error occured while reading " + current + ".", ex);
			}
			finally {
				bfis.close();
			}
		}
	}
	
	// Insert torrent in the queue
	private void insertTorrentInQueue(String name, int length, String filename) {
		
		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		
		try {
			
			session
			.createSQLQuery("INSERT INTO TorrentQueue(name, size, filename) VALUES(?, ?, ?)")
			.setParameter(0, name)
			.setParameter(1, length)
			.setParameter(2, filename)
			.executeUpdate();
			
			tx.commit();
		}
		catch(Exception ex) {
			tx.rollback();
			LOGGER.error("Error while inserting in database <" + filename + ">.", ex);
		}
		finally {
			session.close();
		}
	}
}
