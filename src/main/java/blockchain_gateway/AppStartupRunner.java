package blockchain_gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import blockchain_gateway.adapters.AdapterManager;
import blockchain_gateway.connectionprofiles.ConnectionProfilesManager;
import blockchain_gateway.management.BatchTransactionExecutor;

@Component
public class AppStartupRunner implements ApplicationRunner {

	private static final Logger logger = LoggerFactory.getLogger(AppStartupRunner.class);

	/**
	 *
	 */
	@Override
	public void run(ApplicationArguments args) throws Exception {

		logger.info("Managers Initializations !!");
		var connProfiles = ConnectionProfilesManager.getInstance().getConnectionProfiles();
		AdapterManager.getInstance().initializeBlockchainAdapters(connProfiles);
		BatchTransactionExecutor.getInstance().run();
	}

	@Bean
	public RestTemplate getRestTemplate() {
		return new RestTemplate();
	}
}