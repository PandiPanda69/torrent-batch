package fr.thedestiny.torrent.reader;

import java.util.Iterator;

import lombok.Setter;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import fr.thedestiny.torrent.util.transmission.TransmissionConnector;
import fr.thedestiny.torrent.util.transmission.mapping.TorrentListingResponse;
import fr.thedestiny.torrent.util.transmission.mapping.TorrentListingResponse.Torrent;

/**
 * Lecture des torrents à partir d'un appel RPC à Transmission
 * @author Sébastien
 */
public class TorrentsReader implements ItemReader<Torrent>, ItemStream {

	@Setter
	private TransmissionConnector transmissionConnector;
	
	private Iterator<Torrent> torrentIterator;
	
	public Torrent read() throws Exception, UnexpectedInputException,
			ParseException, NonTransientResourceException {
		
		// Torrent suivant
		try {
			return torrentIterator.next();
		} catch(Exception ex) {
			return null;
		}
	}

	public void close() throws ItemStreamException {	
		// Rien à faire
	}

	public void open(ExecutionContext ctx) throws ItemStreamException {
		try {
			TorrentListingResponse response = transmissionConnector.getTorrentList();
			torrentIterator = response.getTorrents().iterator();
		}
		catch(Exception ex) {
			throw new ItemStreamException(ex);
		}
	}

	public void update(ExecutionContext ctx) throws ItemStreamException {
		// Rien à faire
	}
}
