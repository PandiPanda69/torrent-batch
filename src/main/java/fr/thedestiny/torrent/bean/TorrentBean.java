package fr.thedestiny.torrent.bean;

import lombok.Data;

/**
 * Bean used for database binding
 * @author S�bastien
 */
@Data
public class TorrentBean {

	private Integer id;
	private String name;
	private String hash;
	private String downloadedBytes;
	private String status;
}
