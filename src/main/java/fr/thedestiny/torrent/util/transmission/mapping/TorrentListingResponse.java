package fr.thedestiny.torrent.util.transmission.mapping;

import java.util.List;

import lombok.ToString;
import lombok.Data;
import fr.thedestiny.torrent.util.transmission.request.TransmissionResponse;

/**
 * Dto mappant la r�ponse re�u apr�s demande de la liste des torrents via RPC
 * @author S�bastien
 *
 */
public class TorrentListingResponse extends TransmissionResponse<TorrentListingResponse.Torrent> {

	@Data
	@ToString
	public static class Torrent {
		
		private Integer id;

		private String name;

		private Integer activityDate;
		
		private Integer status;
		
		public boolean isSeeding() {
			return status.equals(6);
		}
	}
	
	public List<TorrentListingResponse.Torrent> getTorrents() {
		return getArguments().get("torrents");
	}
}
