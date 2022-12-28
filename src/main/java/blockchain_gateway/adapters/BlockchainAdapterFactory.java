package blockchain_gateway.adapters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import blockchain_gateway.adapters.fabric.FabricAdapter;
import blockchain_gateway.connectionprofiles.AbstractConnectionProfile;
import blockchain_gateway.connectionprofiles.profiles.FabricConnectionProfile;

public class BlockchainAdapterFactory {

	private static final Logger logger = LoggerFactory.getLogger(BlockchainAdapterFactory.class);

	public BlockchainAdapter createBlockchainAdapter(AbstractConnectionProfile connectionProfile, String blockchainId)
			throws Exception {
		try {
			logger.debug("Creating adapter for the blockchain ID: " + blockchainId);
			if (connectionProfile instanceof FabricConnectionProfile) {
				return createFabricAdapter((FabricConnectionProfile) connectionProfile, blockchainId);
			} else {
				logger.error("Invalid connectionProfile type!");
				return null;
			}
		} catch (Exception e) {
			final String msg = String.format("Error while creating a blockchain adapter for. Details: %s",
					e.getMessage());
			logger.error(msg);
			throw new Exception(msg, e);
		}
	}

	private FabricAdapter createFabricAdapter(FabricConnectionProfile gateway, String blockchainId) {
		return new FabricAdapter(blockchainId);
	}
}
