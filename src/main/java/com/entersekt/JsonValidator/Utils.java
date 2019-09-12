package com.entersekt.JsonValidator;

import java.io.FileInputStream;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

public class Utils {

	final JsonNodeFactory factory = JsonNodeFactory.instance;

	public static JsonNode loadResource(String id) throws JsonProcessingException, IOException {
		// TODO Auto-generated method stub
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.readTree(new FileInputStream(id));
		return rootNode;
	}

}
