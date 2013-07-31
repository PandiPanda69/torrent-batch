package fr.thedestiny.torrent.processor;

import org.springframework.batch.item.ItemProcessor;

import fr.thedestiny.torrent.util.transmission.mapping.TorrentListingResponse.Torrent;

/**
 * Processeur qui vérifie que le torrent est bien en seed et élimine les autres.
 * @author Sébastien
 */
public class StatisticsProcessor implements ItemProcessor<Torrent, Torrent>{

	@Override
	public Torrent process(Torrent in) throws Exception {

		if(in.isSeeding()) {
			return in;
		}
		
		return null;
	}

}
