package fr.thedestiny.torrent.util.transmission.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.codehaus.jackson.map.ObjectMapper;

import fr.thedestiny.torrent.util.transmission.exception.TransmissionRequestArgumentNotFound;

/**
 * Classe modélisant une requête vers transmission
 * @author Sébastien
 */
public class TransmissionRequest {

	private final static String METHOD_FIELD = "method";
	private final static String ARGS_FIELD = "arguments";
		
	@Accessors
	@Getter
	@Setter
	private String method;
	
	@Accessors
	@Getter
	private List<TransmissionRequestArgument> arguments;
	
	private ObjectMapper objectMapper;
	
	/**
	 * Constructeur
	 */
	public TransmissionRequest(ObjectMapper objectMapper) {
		arguments = new ArrayList<TransmissionRequestArgument>();
		
		this.objectMapper = objectMapper;
	}	
	
	/**
	 * Ajoute un nouvel argument
	 * @param name Nom de l'argument à ajouter
	 * @return L'instance du nouvel argument
	 */
	public TransmissionRequestArgument addArgument(String name) {
		
		TransmissionRequestArgument theArg = null;
		
		// Essaie de récupérer l'argument, sinon l'ajoute (ça évite les doublons)
		try {
			theArg = getArgument(name);
		} catch(TransmissionRequestArgumentNotFound ex) {
			theArg = new TransmissionRequestArgument(name);
			arguments.add(theArg);
		}				
		
		return theArg;
	}
	
	/**
	 * Retourne l'argument ayant le nom <em>name</em>.
	 * @param name Nom de l'argument à récupérer
	 * @return L'argument souhaité
	 * @throws TransmissionRequestArgumentNotFound
	 */
	public TransmissionRequestArgument getArgument(String name) throws TransmissionRequestArgumentNotFound {
		for(TransmissionRequestArgument current: arguments) {
			if(current.getArgumentName().equals(name)) {
				return current;
			}
		}
		
		throw new TransmissionRequestArgumentNotFound();
	}
	
	@Override
	public String toString() {
		Map<String, Object> request = new HashMap<String, Object>();
		
		request.put(METHOD_FIELD, method);
		
		// S'il y a des arguments, on les ajoute un à un à la requête
		if(arguments.size() > 0) {
			Map<String, List<String>> requestArgs = new HashMap<String, List<String>>();
			
			for(TransmissionRequestArgument current: arguments) {
				requestArgs.put(current.getArgumentName(), current.getValues());
			}
			
			request.put(ARGS_FIELD, requestArgs);
		} else {
			request.put(ARGS_FIELD, "");
		}
		
		// Génération du JSON et retour du résultat, sinon on retourne NULL avec une trace
		try {
			return objectMapper.writeValueAsString(request);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
