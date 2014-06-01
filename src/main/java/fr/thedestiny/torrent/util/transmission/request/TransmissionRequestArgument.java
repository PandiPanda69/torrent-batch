package fr.thedestiny.torrent.util.transmission.request;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 
 * @author Sébastien
 */
public class TransmissionRequestArgument {

	@Accessors
	@Getter
	@Setter
	private String argumentName;
	
	@Getter
	private List<String> values;
	
	/**
	 * Constructeur
	 * @param name Nom de l'argument
	 */
	public TransmissionRequestArgument(String name) {
		argumentName = name;
		values = new ArrayList<String>();
	}
	
	/**
	 * Ajoute une valeur à l'argument (méthode chaînable)
	 * @param value Valeur à ajouter
	 * @return Instance courante
	 */
	public TransmissionRequestArgument addValue(String value) {
		values.add(value);
		return this;
	}	
}
