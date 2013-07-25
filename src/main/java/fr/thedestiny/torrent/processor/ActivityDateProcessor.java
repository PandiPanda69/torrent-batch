package fr.thedestiny.torrent.processor;

import java.util.Calendar;

import org.springframework.batch.item.ItemProcessor;

import fr.thedestiny.torrent.util.transmission.mapping.TorrentListingResponse.Torrent;

/**
 * Processeur charg� de v�rifier qu'un torrent n'a pas �t� actif depuis plus de X jours.
 * @author S�bastien
 */
public class ActivityDateProcessor implements ItemProcessor<Torrent, Torrent> {

	private Long maximumAllowedIdleTime;
	
	public Torrent process(Torrent in) throws Exception {
		
		if(in.isSeeding() && in.getActivityDate() < maximumAllowedIdleTime) {
			return in;
		}
		
		return null;
	}

	public void setMaximumAllowedIdleTime(Integer idleTime) {
		idleTime *= 24 * 60 * 60;
		
		maximumAllowedIdleTime = Calendar.getInstance().getTimeInMillis() / 1000;
		maximumAllowedIdleTime -= idleTime;		
	}
	
}
