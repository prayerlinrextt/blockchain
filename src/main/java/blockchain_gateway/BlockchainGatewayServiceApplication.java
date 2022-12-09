package blockchain_gateway;

import java.io.File;
import java.util.Collections;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import blockchain_gateway.config.StartupConfig;
import blockchain_gateway.connectionprofiles.ConnectionProfilesManager;

@SpringBootApplication()
public class BlockchainGatewayServiceApplication {
	public static final String startupConfigurationFilePath = System.getenv().getOrDefault("STARTUP_CONFIG",
			"/etc/blockchain_gateway/startup.json");
	public static final String connectionProfileConfigurationFilePath = System.getenv()
			.getOrDefault("CONNECTION_PROFILES_CONFIG", "/etc/blockchain_gateway/connection_profiles.json");
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(BlockchainGatewayServiceApplication.class);

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(BlockchainGatewayServiceApplication.class);
		LoadConfigurations();
		
		Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.valueOf(StartupConfig.getInstance().getLogging()));
		System.setProperty("server.servlet.context-path", StartupConfig.getInstance().getBasePath());
		app.setDefaultProperties(Collections.singletonMap("server.port", StartupConfig.getInstance().getServicePort()));
		app.run(args);
	}

	/**
	 * LoadConfigurations
	 */
	private static void LoadConfigurations() {
		try {
			StartupConfig.getInstance().loadConfigFromFile(startupConfigurationFilePath);
			ConnectionProfilesManager.getInstance()
					.loadConnectionProfilesFromFile(new File(connectionProfileConfigurationFilePath));
		} catch (Exception e) {
			logger.error("Failed to load configurations !", e);
		}
	}
}
