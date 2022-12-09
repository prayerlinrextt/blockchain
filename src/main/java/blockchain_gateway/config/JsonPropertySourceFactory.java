package blockchain_gateway.config;

import java.io.IOException;
import java.util.Map;

import org.springframework.core.env.MapPropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonPropertySourceFactory implements PropertySourceFactory {

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public org.springframework.core.env.PropertySource<?> createPropertySource(String name, EncodedResource resource)
			throws IOException {
		try {
			Map readValue = new ObjectMapper().readValue(resource.getInputStream(), Map.class);
			return new MapPropertySource("json-property", readValue);
		} catch (Exception ex) {
			throw ex;
		}
	}
}