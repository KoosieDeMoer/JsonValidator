package com.entersekt.JsonValidator;

import com.entersekt.validation.ValidationResult;

public interface JsonValidationService {
	ValidationResult validate(String jsonSerialisedObject, String jsonSchemaString) throws Exception;
}
