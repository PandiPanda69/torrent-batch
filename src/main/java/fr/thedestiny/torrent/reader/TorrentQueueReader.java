package fr.thedestiny.torrent.reader;

import java.io.File;
import java.io.FilenameFilter;

import lombok.Setter;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

public class TorrentQueueReader implements ItemReader<String>, ItemStream {

	@Setter
	private String queuePath;
	@Setter
	private String filePattern;
	
	private String[] foundFiles;
	private int index;
	
	@Override
	public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

		if(index < foundFiles.length) {
			return foundFiles[index++];
		}
		
		return null;
	}

	@Override
	public void close() throws ItemStreamException {
		// nop
	}

	@Override
	public void open(ExecutionContext context) throws ItemStreamException {
		
		File path = new File(queuePath);
		
		if(!path.isDirectory()) {
			throw new ItemStreamException("Specified path is not a directory.");
		}
		
		if(!path.canRead()) {
			throw new ItemStreamException("Path cannot be read.");
		}

		index = 0;
		foundFiles = path.list(new FilenameFilter() {
			
			@Override
			public boolean accept(File file, String filename) {
				return filename.matches(filePattern);
			}
		});
	}

	@Override
	public void update(ExecutionContext context) throws ItemStreamException {
		// nop
	}

}
