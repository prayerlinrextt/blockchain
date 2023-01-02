package blockchain_gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = CustomMedicalRecordStatusDeserializer.class)
public class ItemStatus {

	private String id;

	@JsonProperty("id")
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@JsonProperty("status")
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	private Status status;

	public ItemStatus(String id) {
		this.id = id;
		status = Status.NOT_FOUND;
	}

}
