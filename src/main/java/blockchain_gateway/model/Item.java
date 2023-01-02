package blockchain_gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Item {

	private String id;

	@JsonProperty("id")
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@JsonProperty("data_hash")
	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	@JsonProperty("since")
	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	@JsonProperty("data")
	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	private String hash;

	private String time;

	private String data;

}
