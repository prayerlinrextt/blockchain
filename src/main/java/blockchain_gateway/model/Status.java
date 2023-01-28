package blockchain_gateway.model;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = CustomItemStatusDeserializer.class)
public enum Status {

	CREATED("Created"), PENDING("Pending"), NOT_FOUND("Not Found"), VALID("Valid"), IN_VALID("In Valid");

	private String status;

	Status(String status) {
		this.status = status;
	}

	public String getName() {
		return this.name();
	}

	@JsonValue
	public String getStatus() {
		return status;
	}

}
