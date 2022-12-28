package blockchain_gateway.adapters;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import blockchain_gateway.connectionprofiles.AbstractConnectionProfile;
import blockchain_gateway.connectionprofiles.ConnectionProfilesManager;

public class AdapterManager {

	private BlockchainAdapterFactory factory = new BlockchainAdapterFactory();
	private static AdapterManager instance = null;
	private static final Logger logger = LoggerFactory.getLogger(AdapterManager.class);
	private final Map<String, Pair<BlockchainAdapter, AbstractConnectionProfile>> map = Collections
			.synchronizedMap(new HashMap<>());
	public BlockchainAdapter m_BlockchainAdapter;
	private Iterator<String> it;

	public void finalize() throws Throwable {

	}

	private AdapterManager() {
		it = map.keySet().iterator();
	}

	/**
	 * 
	 * @param blockchainId
	 * @exception BlockchainIdNotFoundException,BlockchainNodeUnreachableException
	 */
	public BlockchainAdapter getAdapter(String blockchainId) throws Exception {
		AbstractConnectionProfile connectionProfile = ConnectionProfilesManager.getInstance().getConnectionProfiles()
				.get(blockchainId);
		// no connection profile!
		if (connectionProfile == null) {
			final String msg = String.format("blockchain-id <%s> does not exist!", blockchainId);
			logger.error(msg);
			throw new Exception(msg);
		}

		// we already have an adapter for it
		if (map.containsKey(blockchainId)) {
			Pair<BlockchainAdapter, AbstractConnectionProfile> result = map.get(blockchainId);
			// is the connection profile still the same?
			if (result.getRight().equals(connectionProfile)) {
				return map.get(blockchainId).getLeft();
			}
		}

		try {
			final BlockchainAdapter adapter = factory.createBlockchainAdapter(connectionProfile, blockchainId);
			map.put(blockchainId, ImmutablePair.of(adapter, connectionProfile));
			return Objects.requireNonNull(adapter);
		} catch (Exception e) {
			throw new Exception("Failed to create a blockchain adapter. Reason: " + e.getMessage());
		}
	}

	public static AdapterManager getInstance() {
		if (instance == null) {
			instance = new AdapterManager();
		}
		return instance;
	}

	/**
	 * 
	 * @param conn_profiles
	 * @throws Exception
	 */
	public void initializeBlockchainAdapters(Map<String, AbstractConnectionProfile> connMap) throws Exception {
		try {
			logger.debug("Initializing blockchain adapters ..");
			for (Map.Entry<String, AbstractConnectionProfile> entry : connMap.entrySet()) {
				final BlockchainAdapter adapter = factory.createBlockchainAdapter(entry.getValue(), entry.getKey());
				map.put(entry.getKey(), ImmutablePair.of(adapter, entry.getValue()));

			}
		} catch (Exception e) {
			throw new Exception("Failed to create a block chain adapter. Reason: " + e.getMessage());
		}

	}

	public BlockchainAdapter getNextAdapter() throws Exception {
		try {
			if (!it.hasNext()) {
				it = map.keySet().iterator();
			}
			String blockchainId = it.next();

			// we already have an adapter for it
			if (map.containsKey(blockchainId)) {
				Pair<BlockchainAdapter, AbstractConnectionProfile> result = map.get(blockchainId);
				return result.getLeft();
			} else {
				return null;
			}
		} catch (Exception e) {
			throw new Exception("Failed to get next adapter. Reason: " + e.getMessage());
		}
	}
}
