package com.jshnd.virp.jackson;

import com.jshnd.virp.Transformer;
import com.jshnd.virp.exception.VirpOperationException;
import org.codehaus.jackson.map.ObjectMapper;

public class JacksonTransformer implements Transformer<String, Object> {

	private static ObjectMapper mapper = new ObjectMapper();

	private Class<?> classFor;

	public JacksonTransformer(Class<?> classFor) {
		this.classFor = classFor;
	}

	@Override
	public Object valueForObject(String valueInCassandra) {
		try {
			return mapper.readValue(valueInCassandra, classFor);
		} catch (Exception e) {
			throw new VirpOperationException("Unable to convert from json", e);
		}
	}

	@Override
	public String valueForCassandra(Object valueInObject) {
		try {
			return mapper.writeValueAsString(valueInObject);
		} catch (Exception e) {
			throw new VirpOperationException("Unable to convert to json", e);
		}
	}

	@Override
	public Class<? extends String> cassandraValueClass() {
		return String.class;
	}
}
