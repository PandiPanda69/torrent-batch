package fr.thedestiny.torrent.util.transmission.mapping;

import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.Data;
import fr.thedestiny.torrent.util.transmission.request.TransmissionResponse;

/**
 * Dto mappant la réponse reçu après demande de la liste des torrents via RPC
 * @author Sébastien
 *
 */
public class TorrentListingResponse extends TransmissionResponse<TorrentListingResponse.Torrent> {

	@Data
	@ToString
	public static class Torrent {
		
		private Integer id;

		private String name;
		
		private String hashString;

		private Integer activityDate;
		
		private Integer status;
		
		private Double uploadRatio;
		
		private Long uploadedEver;
		
		@Getter(AccessLevel.NONE)
		private Long totalSize;
		
		@Getter(AccessLevel.NONE)
		private Long leftUntilDone;
		
		public boolean isSeeding() {
			return status.equals(6);
		}
		
		public Long getSize() {
			return totalSize - leftUntilDone;
		}
	}
	
	public List<TorrentListingResponse.Torrent> getTorrents() {
		return getArguments().get("torrents");
	}
}
