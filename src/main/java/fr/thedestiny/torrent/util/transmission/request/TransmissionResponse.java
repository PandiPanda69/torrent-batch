package fr.thedestiny.torrent.util.transmission.request;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public abstract class TransmissionResponse<T> {

	private String result;

	private Map<String, List<T>> arguments;
}
