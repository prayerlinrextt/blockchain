package blockchain_gateway.connectionprofiles.profiles;

import java.util.Properties;

import blockchain_gateway.connectionprofiles.AbstractConnectionProfile;

public class FabricConnectionProfile extends AbstractConnectionProfile {

	private static final String PREFIX = "hyperledger.fabric.";
	private static final String WALLET_PATH = PREFIX + "walletPath";
	private static final String USER_NAME = PREFIX + "userName";
	private static final String CONNECTION_PROFILE_PATH = PREFIX + "connectionProfilePath";
	private static final String MSP_ID = PREFIX + "mspID";
	private static final String MSP_PATH = PREFIX + "mspID";
	private String connectionProfilePath;
	private String userName;
	private String walletPath;
	private String mspID;

	public String getMspID() {
		return mspID;
	}

	public void setMspID(String mspID) {
		this.mspID = mspID;
	}

	public String getMspPath() {
		return mspPath;
	}

	public void setMspPath(String mspPath) {
		this.mspPath = mspPath;
	}

	private String mspPath;

	public FabricConnectionProfile() {
	}

	public FabricConnectionProfile(String walletPath, String userName, String connectionProfilePath) {
		this.walletPath = walletPath;
		this.userName = userName;
		this.connectionProfilePath = connectionProfilePath;
	}

	public String getWalletPath() {
		return walletPath;
	}

	public void setWalletPath(String walletPath) {
		this.walletPath = walletPath;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getConnectionProfilePath() {
		return connectionProfilePath;
	}

	public void setConnectionProfilePath(String connectionProfilePath) {
		this.connectionProfilePath = connectionProfilePath;
	}

	@Override
	public Properties getAsProperties() {
		final Properties result = super.getAsProperties();
		result.setProperty(WALLET_PATH, this.walletPath);
		result.setProperty(USER_NAME, this.userName);
		result.setProperty(CONNECTION_PROFILE_PATH, this.connectionProfilePath);
		result.setProperty(MSP_ID, this.mspID);
		result.setProperty(MSP_PATH, this.mspPath);
		return result;
	}

}
