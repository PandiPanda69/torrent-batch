package fr.thedestiny.torrent.processor;

import java.util.List;

import lombok.Setter;

import org.springframework.batch.item.ItemProcessor;

public class TorrentQueueProcessor implements ItemProcessor<String, String> {

	@Setter
	private List<String> currentTorrentQueue;
	
	@Override
	public String process(String input) throws Exception {

		// No torrent in queue or input is not in the queue
		if(currentTorrentQueue.isEmpty() || !currentTorrentQueue.contains(input)) {
			return input;
		}
		
		// torrent is in the queue, let reduce list size by deleting torrent from list
		// a file cannot be twice in the same directory
		currentTorrentQueue.remove(input);
		
		return null;
	}
}
