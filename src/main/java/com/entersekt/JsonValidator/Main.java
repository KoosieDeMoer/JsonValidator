package com.entersekt.JsonValidator;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

public class Main {

	public static void main(final String... args) throws IOException, ProcessingException {
		final JsonNode fstabSchema = Utils.loadResource("fstab.json");
		final JsonNode good = Utils.loadResource("fstab-good.json");
		final JsonNode bad = Utils.loadResource("fstab-bad.json");
		final JsonNode bad2 = Utils.loadResource("fstab-bad2.json");

		final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();

		final JsonSchema schema = factory.getJsonSchema(fstabSchema);

		ProcessingReport report;

		report = schema.validate(good);
		System.out.println(report);

		report = schema.validate(bad);
		System.out.println(report);
		System.out.println(report.iterator().next().getMessage());

		report = schema.validate(bad2);
		System.out.println(report);
	}
}
