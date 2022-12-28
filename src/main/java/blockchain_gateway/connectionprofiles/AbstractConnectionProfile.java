package blockchain_gateway.connectionprofiles;

import java.util.Properties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import blockchain_gateway.connectionprofiles.profiles.FabricConnectionProfile;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({ @JsonSubTypes.Type(value = FabricConnectionProfile.class, name = "fabric") })
public class AbstractConnectionProfile {

	public AbstractConnectionProfile() {

	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		AbstractConnectionProfile that = (AbstractConnectionProfile) o;
		return this.getAsProperties().equals(that.getAsProperties());
	}

	@Override
	public int hashCode() {
		return this.getAsProperties().hashCode();
	}

	public Properties getAsProperties() {
		Properties result = new Properties();
		return result;
	}

}
