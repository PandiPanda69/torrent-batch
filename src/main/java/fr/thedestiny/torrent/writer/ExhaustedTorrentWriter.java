package fr.thedestiny.torrent.writer;

import java.util.List;

import org.springframework.batch.item.file.FlatFileItemWriter;

import fr.thedestiny.torrent.util.transmission.mapping.TorrentListingResponse.Torrent;

public class ExhaustedTorrentWriter extends FlatFileItemWriter<Torrent> {

	public ExhaustedTorrentWriter() {		
	}
	
	public void write(List<? extends Torrent> in) throws Exception {
		
		for(Torrent current : in) {
			System.out.println(current);
		}
	}

}
