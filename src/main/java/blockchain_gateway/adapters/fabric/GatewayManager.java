package blockchain_gateway.adapters.fabric;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Identities;
import org.hyperledger.fabric.gateway.Identity;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import blockchain_gateway.connectionprofiles.AbstractConnectionProfile;
import blockchain_gateway.connectionprofiles.ConnectionProfilesManager;
import blockchain_gateway.connectionprofiles.profiles.FabricConnectionProfile;

public class GatewayManager {
	private static final Logger log = LoggerFactory.getLogger(GatewayManager.class);
	private static GatewayManager instance;
	private Map<String, Gateway> gateways;
	private Map<String, Network> channels;
	private Map<String, Contract> contracts;

	private GatewayManager() {
		gateways = new HashMap<>();
		channels = new HashMap<>();
		contracts = new HashMap<>();
	}

	public Gateway getGateway(String blockchainId) throws Exception {
		log.debug("Get the gateway for the blockchain ID: " + blockchainId);
		if (gateways.containsKey(blockchainId)) {
			return gateways.get(blockchainId);
		}

		AbstractConnectionProfile profile = ConnectionProfilesManager.getInstance().getConnectionProfiles()
				.get(blockchainId);

		if (!(profile instanceof FabricConnectionProfile)) {
			throw new Exception();
		}
		final String walletPath = ((FabricConnectionProfile) profile).getWalletPath();
		final String networkConfigPath = ((FabricConnectionProfile) profile).getConnectionProfilePath();
		final String user = ((FabricConnectionProfile) profile).getUserName();
		final String mspID = ((FabricConnectionProfile) profile).getMspID();
		final String mspPath = ((FabricConnectionProfile) profile).getMspPath();
		// Load an existing wallet holding identities used to access the network.
		Path walletDirectory = Paths.get(walletPath);

		Wallet wallet = null;
		try {
			wallet = Wallets.newFileSystemWallet(walletDirectory);

			Path credentialPath = Paths.get(mspPath);

			Path certificatePath = credentialPath.resolve(Paths.get("signcerts", "cert.pem"));

			Path privateKeyPath = credentialPath.resolve(Paths.get("keystore"));
			File dir = new File(privateKeyPath.toString());
			// Get private key file in the directory
			File[] files = dir.listFiles((d, name) -> name.endsWith("_sk"));
			privateKeyPath = Path.of(files[0].getAbsolutePath());

			X509Certificate certificate = readX509Certificate(certificatePath);
			PrivateKey privateKey = getPrivateKey(privateKeyPath);

			Identity identity = Identities.newX509Identity(mspID, certificate, privateKey);
			wallet.put(user, identity);

			// Path to a connection profile describing the network.
			Path networkConfigFile = Paths.get(networkConfigPath);

			// Configure the gateway connection used to access the network.
			Gateway result = Gateway.createBuilder().identity(wallet, user).networkConfig(networkConfigFile).connect();
			gateways.put(blockchainId, result);

			return result;
		} catch (IOException e) {
			throw new Exception("Cannot create Fabric gateway. Reason: " + e.getMessage());
		}
	}

	public Network getChannel(String blockchainId, String channelName) throws Exception {
		log.debug("Get the channel for the blockchain ID: " + blockchainId + " and the channel name: " + channelName);
		try {
			if (channels.containsKey(blockchainId + ":" + channelName)) {
				return channels.get(blockchainId + ":" + channelName);
			}

			Network channel = this.getGateway(blockchainId).getNetwork(channelName);
			channels.put(blockchainId + ":" + channelName, channel);
			return channel;
		} catch (Exception e) {
			throw new Exception("Cannot get the channel. Reason: " + e.getMessage());
		}
	}

	public Contract getContract(String blockchainId, String channelName, String chaincodeName) throws Exception {
		log.debug("Get the channel for the blockchain ID: " + blockchainId + ", channel name: " + channelName
				+ " and the contract name: " + chaincodeName);
		try {
			if (contracts.containsKey(blockchainId + ":" + channelName + ":" + chaincodeName)) {
				return contracts.get(blockchainId + ":" + channelName + ":" + chaincodeName);
			}

			Contract contract = this.getChannel(blockchainId, channelName).getContract(chaincodeName);
			contracts.put(blockchainId + ":" + channelName + ":" + chaincodeName, contract);
			return contract;
		} catch (Exception e) {
			throw new Exception("Cannot get the contract. Reason: " + e.getMessage());
		}

	}

	public static GatewayManager getInstance() {
		if (instance == null) {
			instance = new GatewayManager();
		}
		return instance;
	}

	/**
	 * This method will read the X509Certificate.
	 * 
	 * @param certificatePath
	 * @throws Exception
	 */
	private static X509Certificate readX509Certificate(final Path certificatePath) throws Exception {
		try (Reader certificateReader = Files.newBufferedReader(certificatePath, StandardCharsets.UTF_8)) {
			return Identities.readX509Certificate(certificateReader);
		} catch (IOException e) {
			throw new Exception("Cannot read X509Certificate. Reason: " + e.getMessage());
		}
	}

	/**
	 * This method will get the PrivateKey.
	 * 
	 * @param privateKeyPath
	 * @throws Exception
	 */
	private static PrivateKey getPrivateKey(final Path privateKeyPath) throws Exception {
		try (Reader privateKeyReader = Files.newBufferedReader(privateKeyPath, StandardCharsets.UTF_8)) {
			return Identities.readPrivateKey(privateKeyReader);
		} catch (Exception e) {
			throw new Exception("Cannot get private key. Reason: " + e.getMessage());
		}
	}
}
