package blockchain_gateway.model;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class CustomItemStatusDeserializer extends StdDeserializer<ItemStatus> {

	private static final long serialVersionUID = -3287725521124570024L;

	public CustomItemStatusDeserializer() {
		this(null);
	}

	public CustomItemStatusDeserializer(Class<?> c) {
		super(c);
	}

	@Override
	public ItemStatus deserialize(JsonParser jsonParser, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		JsonNode node = jsonParser.getCodec().readTree(jsonParser);

		String id = node.get("id").asText();
		ItemStatus obj = new ItemStatus(id);
		String status = node.get("status").asText();
		if (status.equals("Valid"))
			obj.setStatus(Status.VALID);
		else if (status.equals("In Valid") || status.equals("Invalid"))
			obj.setStatus(Status.IN_VALID);
		else if (status.equals("Not found"))
			obj.setStatus(Status.NOT_FOUND);
		return obj;
	}
}
