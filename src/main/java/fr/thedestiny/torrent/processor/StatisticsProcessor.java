package fr.thedestiny.torrent.processor;

import org.springframework.batch.item.ItemProcessor;

import fr.thedestiny.torrent.util.transmission.mapping.TorrentListingResponse.Torrent;

/**
 * Processeur qui v�rifie que le torrent est bien en seed et �limine les autres.
 * @author S�bastien
 */
public class StatisticsProcessor implements ItemProcessor<Torrent, Torrent>{

	@Override
	public Torrent process(Torrent in) throws Exception {

		if(in.isSeeding() || in.isFinished()) {
			return in;
		}
		
		return null;
	}

}
