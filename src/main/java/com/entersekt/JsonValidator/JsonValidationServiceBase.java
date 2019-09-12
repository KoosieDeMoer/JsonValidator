package com.entersekt.JsonValidator;

import com.entersekt.json.JsonSerialisationService;
import com.google.inject.Inject;

public abstract class JsonValidationServiceBase implements JsonValidationService {

	private JsonSerialisationService jsonSerialisationService;

	@Inject
	public void setJsonSerialisationService(JsonSerialisationService jsonSerialisationService) {
		this.jsonSerialisationService = jsonSerialisationService;
	}

}
