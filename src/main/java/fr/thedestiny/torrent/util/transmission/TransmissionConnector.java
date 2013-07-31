package fr.thedestiny.torrent.util.transmission;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import lombok.Setter;

import org.codehaus.jackson.map.ObjectMapper;

import fr.thedestiny.torrent.util.transmission.exception.TransmissionRequestStringifyFailed;
import fr.thedestiny.torrent.util.transmission.mapping.TorrentListingResponse;
import fr.thedestiny.torrent.util.transmission.request.TransmissionRequest;

/**
 * Communication avec transmission via des appels RPC
 * @author Sébastien
 */
public class TransmissionConnector {

	private final static String TRANSMISSION_RPC = "/transmission/rpc";
	
	private final static String TRANSMISSION_SESSION_ID_HEADER_FIELD = "X-Transmission-Session-Id";
	private final static String CONTENT_TYPE_HEADER_FIELD = "Content-Type";
	private final static String CONTENT_LENGTH_HEADER_FIELD = "Content-Length";
	
	@Setter
	private ObjectMapper objectMapper;
	
	@Setter
	private String transmissionAddr;
	
	@Setter
	private Integer transmissionPort;
	
	private String sessionId;
	
	/**
	 * Constructeur
	 * @param addr Adresse du serveur
	 * @param port Port du serveur
	 */
	public TransmissionConnector(String addr, Integer port) {
		this.transmissionAddr = addr;
		this.transmissionPort = port;
	}
	
	/**
	 * Récupération de la liste des torrents
	 * @return Objet mappé
	 * @throws IOException
	 * @throws TransmissionRequestStringifyFailed
	 */
	public TorrentListingResponse getTorrentList() throws IOException, TransmissionRequestStringifyFailed {
		
		TransmissionRequest request = new TransmissionRequest(objectMapper);
		request.setMethod("torrent-get");
		request.addArgument("fields").addValue("id")
									.addValue("name")
									.addValue("activityDate")
									.addValue("status")
									.addValue("uploadRatio")
									.addValue("uploadedEver")
									.addValue("totalSize")
									.addValue("leftUntilDone")
									.addValue("hashString");
		
		return postRequest(request, TorrentListingResponse.class);
	}
	
	/**
	 * Met à jour le sessionId permettant d'interroger Transmission
	 * @throws IOException Impossible d'ouvrir l'URL
	 */
	public void refreshSessionId() throws IOException {
		URL url = new URL("http", transmissionAddr, transmissionPort, TRANSMISSION_RPC);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		
		try {
			connection.getInputStream();
		}
		catch(IOException ex) {
			// Rien
		}
		
		// Récupération du sessionId dans l'en-tête de la requête
		sessionId = connection.getHeaderField(TRANSMISSION_SESSION_ID_HEADER_FIELD);
	}
	
	/**
	 * Envoi une requête POST à Transmission
	 * @param request Contenu de la requête à envoyer
	 * @return La réponse mappée
	 * @throws IOException
	 * @throws TransmissionRequestStringifyFailed
	 */
	private <T> T postRequest(TransmissionRequest request, Class<T> clazz) throws IOException, TransmissionRequestStringifyFailed {
		
		// Si le sessionId n'a pas encore été déterminé
		if(sessionId == null) {
			refreshSessionId();
		}
		
		// Stringify de la requête
		String jsonRequest = request.toString();
		if(jsonRequest == null) {
			throw new TransmissionRequestStringifyFailed();
		}
		
		// Envoi de l'appel RPC
		URL url = new URL("http", transmissionAddr, transmissionPort, TRANSMISSION_RPC);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		
		connection.setRequestMethod("POST");
		connection.addRequestProperty(TRANSMISSION_SESSION_ID_HEADER_FIELD, sessionId);
		connection.addRequestProperty(CONTENT_TYPE_HEADER_FIELD, "json");
		connection.addRequestProperty(CONTENT_LENGTH_HEADER_FIELD, String.valueOf(jsonRequest.length()));

		connection.setDoOutput(true);

		OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
		writer.write(jsonRequest);
		writer.flush();

		writer.close();

		// Lecture de la réponse
		String content = null;
		try {
			content = this.getUrlContent(connection.getInputStream());
		}
		catch(IOException ex) {
			content = this.getUrlContent(connection.getErrorStream());			
		}

		return objectMapper.readValue(content, clazz);
	}
	
	/**
	 * Lecture de la réponse d'une requête
	 * @param in Flux à lire
	 * @return La réponse sous forme d'une chaîne de caractère
	 * @throws IOException
	 */
	private String getUrlContent(InputStream in) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuffer content = new StringBuffer();
		
		String currentLine = reader.readLine();
		while(currentLine != null) {
			content.append(currentLine);
			currentLine = reader.readLine();			
		} 
		
		reader.close();
		return content.toString();
	}
}
