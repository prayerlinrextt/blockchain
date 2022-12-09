package blockchain_gateway.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import blockchain_gateway.model.SiteEndpoints;

public class StartupConfig {

	private static final Logger logger = LoggerFactory.getLogger(StartupConfig.class);
	private static StartupConfig instance;
	private ObjectReader reader;

	private String logging;

	private Integer servicePort;

	private String basePath;
	
	private Integer threadPoolSize;

	public String getLogging() {
		return logging;
	}

	public void setLogging(String logging) {
		this.logging = logging;
	}

	public Integer getServicePort() {
		return servicePort;
	}

	public void setServicePort(Integer servicePort) {
		this.servicePort = servicePort;
	}

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public Integer getCommitSize() {
		return commitSize;
	}

	public void setCommitSize(Integer commitSize) {
		this.commitSize = commitSize;
	}

	public Integer getCommitTimeOut() {
		return commitTimeOut;
	}

	public void setCommitTimeOut(Integer commitTimeOut) {
		this.commitTimeOut = commitTimeOut;
	}

	public String getErrorTopic() {
		return errorTopic;
	}

	public void setErrorTopic(String errorTopic) {
		this.errorTopic = errorTopic;
	}

	public List<SiteEndpoints> getGroup() {
		return group;
	}

	public void setGroup(List<SiteEndpoints> group) {
		this.group = group;
	}
	
	public Integer getThreadPoolSize() {
		return threadPoolSize;
	}

	private Integer commitSize;

	private Integer commitTimeOut;

	private String bootStraps;

	private String errorTopic;

	private List<SiteEndpoints> group;

	public StartupConfig() {
		group = new ArrayList<SiteEndpoints>();
		this.reader = new ObjectMapper().readerFor(new TypeReference<StartupConfig>() {
		});
	}

	@JsonProperty("service")
	private void unpackService(Map<String, Object> service) {
		this.servicePort = (Integer) service.get("port");
		this.basePath = (String) service.get("basepath");
	}

	@JsonProperty("batch_commit")
	private void unpackCommit(Map<String, Object> commit) {
		this.commitSize = (Integer) commit.get("size");
		this.commitTimeOut = (Integer) commit.get("timeout_in_ms");
	}

	public static StartupConfig getInstance() {
		if (instance == null) {
			instance = new StartupConfig();
		}

		return instance;
	}

	public void loadConfigFromFile(
			@Value("/etc/blockchain_gateway/startup.json") String startupConfigurationFilePath) throws Exception {
		logger.debug("Loading startup configuration from file!");
		File file = new File(startupConfigurationFilePath);
		JsonFactory jfactory = this.reader.getFactory();
		JsonParser jParser;
		try {
			jParser = jfactory.createParser(file);
			JsonNode node = this.reader.readTree(jParser);
			this.logging = node.get("logging").asText("warning");

			this.servicePort = node.get("service").get("port").asInt(8080);
			this.basePath = node.get("service").get("basepath").asText("/api/v1");
			this.commitSize = node.get("batch_commit").get("size").asInt(100);
			this.commitTimeOut = node.get("batch_commit").get("timeout_in_ms").asInt(100);

			
			this.threadPoolSize = node.get("thread_pool_size").asInt(5);

			ObjectReader siteMapper = new ObjectMapper().readerFor(new TypeReference<List<SiteEndpoints>>() {
			});

			jParser.close();
		} catch (IOException e) {
			throw new Exception("Failed to load startup configuration. Reason: " + e.getMessage());
		}
	}

	public String getBootStraps() {
		return bootStraps;
	}

	public void setBootStraps(String bootStraps) {
		this.bootStraps = bootStraps;
	}
}
