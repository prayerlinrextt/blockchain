package blockchain_gateway.connectionprofiles;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

public class ConnectionProfilesManager {

	private Map<String, AbstractConnectionProfile> connectionProfilesMap;
	private static ConnectionProfilesManager instance;
	private static final Logger log = LoggerFactory.getLogger(ConnectionProfilesManager.class);
	private ObjectReader reader;
	private ObjectWriter writer;

	private ConnectionProfilesManager() {
		this.connectionProfilesMap = new HashMap<>();
		this.reader = new ObjectMapper().readerFor(new TypeReference<Map<String, AbstractConnectionProfile>>() {
		});
		this.writer = new ObjectMapper().writerFor(new TypeReference<Map<String, AbstractConnectionProfile>>() {
		});
	}

	public Map<String, AbstractConnectionProfile> getConnectionProfiles() {
		return this.connectionProfilesMap;
	}

	public String getConnectionProfilesAsJson() throws JsonProcessingException {
		return this.writer.writeValueAsString(this.connectionProfilesMap);
	}

	public static ConnectionProfilesManager getInstance() {
		if (instance == null) {
			instance = new ConnectionProfilesManager();
		}
		return instance;
	}

	public void loadConnectionProfiles(Map<String, AbstractConnectionProfile> newMap) {
		this.connectionProfilesMap.putAll(newMap);
	}

	public void resetConnectionProfiles() {
		this.connectionProfilesMap.clear();
	}

	public void loadConnectionProfilesFromFile(File file) throws Exception {
		log.debug("Loading connection profiles from the file!");
		try {
			Map<String, AbstractConnectionProfile> newMap = this.reader.readValue(file);
			this.loadConnectionProfiles(newMap);
		} catch (IOException e) {
			log.error("Failed to load connection profiles from the file!", e);
			throw e;
		}
	}

	public void loadConnectionProfilesFromJson(String jsonString) throws Exception {
		log.debug("Loading connection profiles from json string !");
		try {
			Map<String, AbstractConnectionProfile> newMap = reader.readValue(jsonString);
			this.loadConnectionProfiles(newMap);
		} catch (IOException e) {
			log.error("Failed to load connection profiles from the string!", e);
			throw e;
		}
	}

	public Set<String> getblockchainIds() {
		return connectionProfilesMap.keySet();
	}
}
