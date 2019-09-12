package com.entersekt.JsonValidator;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.entersekt.validation.ValidationResult;
import com.entersekt.validation.ValidationResultCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

public class JsonValidationServiceHuggsBosunImpl extends JsonValidationServiceBase implements JsonValidationService {

	public ValidationResult validate(String jsonSerialisedObject, String jsonSchemaString) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonObject = mapper.readTree(jsonSerialisedObject);
		JsonNode jsonSchemaObject = mapper.readTree(jsonSchemaString);
		final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();

		final JsonSchema schema = factory.getJsonSchema(jsonSchemaObject);

		ProcessingReport report = schema.validate(jsonObject);
		if (report.isSuccess()) {
			if (jsonSerialisedObject.contains(ValidationResult.INCOMPLETENESS_INDICATOR)
					|| jsonSerialisedObject.equals(ValidationResult.INCOMPLETENESS_DATE_INDICATOR)) {
				String[] validationResults = parseJson(jsonSerialisedObject);
				// remove the duplicates
				validationResults = new HashSet<>(Arrays.asList(validationResults)).toArray(new String[] {});
				return new ValidationResult(ValidationResultCode.INCOMPLETE, validationResults);
			} else {
				return new ValidationResult(ValidationResultCode.COMPLETE, new String[] { "Some good news" });
			}
		} else {
			// Set remove the duplicates
			Set<String> messageList = new HashSet<>();
			for (ProcessingMessage message : report) {
				messageList.add(message.getMessage());
			}
			return new ValidationResult(ValidationResultCode.INVALID, messageList.toArray(new String[] {}));
		}
	}

	static String[] parseJson(String json) throws IOException {

		List<String> messageList = new ArrayList<>();
		JsonReader reader = new JsonReader(new StringReader(json));
		reader.setLenient(true);
		while (true) {
			JsonToken token = reader.peek();
			switch (token) {
			case BEGIN_ARRAY:
				reader.beginArray();
				break;
			case END_ARRAY:
				reader.endArray();
				break;
			case BEGIN_OBJECT:
				reader.beginObject();
				break;
			case END_OBJECT:
				reader.endObject();
				break;
			case NAME:
				reader.nextName();
				break;
			case STRING:
				String s = reader.nextString();
				if (s.contains(ValidationResult.INCOMPLETENESS_INDICATOR)
						|| s.equals(ValidationResult.INCOMPLETENESS_DATE_INDICATOR)) {
					messageList.add(print(reader.getPath(), quote(s)));
				}
				break;
			case NUMBER:
				String n = reader.nextString();
				if (n.contains(ValidationResult.INCOMPLETENESS_INDICATOR)) {
					messageList.add(print(reader.getPath(), n));
				}
				break;
			case BOOLEAN:
				boolean b = reader.nextBoolean();
				// incomplete boolean
				break;
			case NULL:
				reader.nextNull();
				break;
			case END_DOCUMENT:
				reader.close();
				return messageList.toArray(new String[] {});
			}
		}
	}

	static String print(String path, Object value) {
		path = path.substring(2);
		path = PATTERN.matcher(path).replaceAll("");
		return path + ": " + value;
	}

	static private String quote(String s) {
		return new StringBuilder().append('"').append(s).append('"').toString();
	}

	static final String REGEX = "\\[[0-9]+\\]";
	static final Pattern PATTERN = Pattern.compile(REGEX);

}
