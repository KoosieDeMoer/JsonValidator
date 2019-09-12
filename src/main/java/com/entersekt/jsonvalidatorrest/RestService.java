package com.entersekt.jsonvalidatorrest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.entersekt.JsonValidator.JsonValidationService;
import com.entersekt.JsonValidator.Utils;
import com.entersekt.json.JsonSerialisationService;
import com.entersekt.persistence.PersistenceService;
import com.entersekt.validation.ValidationResult;
import com.fasterxml.jackson.databind.JsonNode;

@Path("/Validator")
@Api(value = "/Validator")
public class RestService {

	private static final String SAMPLE_JSON_SCHEMA_FILENAME = "fstab.json";

	private static final Logger log = LoggerFactory.getLogger(RestService.class);

	private static final JsonValidationService validationService = GuiceBindingsModule.injector
			.getInstance(JsonValidationService.class);
	private static PersistenceService schemaPersistenceService;
	private static PersistenceService metaSchemaPersistenceService;
	private static PersistenceService blankFormsPersistenceService;
	private static final JsonSerialisationService jsonSerialisationService = GuiceBindingsModule.injector
			.getInstance(JsonSerialisationService.class);

	public RestService() {
		initialisePersistenceService();
	}

	@POST
	@Path("validate/{id}")
	@ApiOperation(value = "Validates a document", notes = "Pass in a json serialised object as a String")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Validation successful") })
	public Response validate(
			@ApiParam(value = "Name of json schema the doc should be validated against", required = true) @PathParam("id") String id,
			@ApiParam(value = "Cleartext doc to be validated", required = true) String jsonSerialisedObject)
			throws Exception {
		ValidationResult validationResult = validationService.validate(jsonSerialisedObject,
				schemaPersistenceService.readDoc(id));
		return Response.status(Response.Status.OK).entity(jsonSerialisationService.serialise(validationResult)).build();
	}

	@PUT
	@Path("schemas/{id}")
	@ApiOperation(value = "Stores a json schema definition")
	@Consumes(MediaType.TEXT_PLAIN)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Doc created") })
	public Response write(String doc,
			@ApiParam(value = "A reference to the schema to be written", required = true) @PathParam("id") String id) {
		schemaPersistenceService.blatDoc(id, doc);
		log.info("Doc with id='" + id + "' received and persisted");

		return Response.status(Response.Status.OK).build();
	}

	@GET
	@Path("schemas/{id}")
	@ApiOperation(value = "Retrieves a json schema definition")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Doc retrieved") })
	public Response read(
			@ApiParam(value = "A reference to the schema to be read", required = true) @PathParam("id") String id) {
		return Response.status(Response.Status.OK).entity(schemaPersistenceService.readDoc(id)).build();
	}

	@GET
	@Path("schemas")
	@ApiOperation(value = "Retrieves a list of available schema definitions")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
	public Response listSchemas() {
		return Response.status(Response.Status.OK).entity(schemaPersistenceService.listDocs()).build();
	}

	@PUT
	@Path("meta_schemas/{id}")
	@ApiOperation(value = "Stores a json meta schema definition")
	@Consumes(MediaType.TEXT_PLAIN)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Doc created") })
	public Response writeMetaSchema(
			String doc,
			@ApiParam(value = "A reference to the meta schema to be written", required = true) @PathParam("id") String id) {
		metaSchemaPersistenceService.blatDoc(id, doc);
		log.info("Doc with id='" + id + "' received and persisted");

		return Response.status(Response.Status.OK).build();
	}

	@GET
	@Path("meta_schemas/{id}")
	@ApiOperation(value = "Retrieves a json meta schema definition")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Doc retrieved") })
	public Response readMetaSchema(
			@ApiParam(value = "A reference to the meta schema to be read", required = true) @PathParam("id") String id) {
		return Response.status(Response.Status.OK).entity(metaSchemaPersistenceService.readDoc(id)).build();
	}

	@GET
	@Path("meta_schemas")
	@ApiOperation(value = "Retrieves a list of available meta schema definitions")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
	public Response listMetaSchemas() {
		return Response.status(Response.Status.OK).entity(metaSchemaPersistenceService.listDocs()).build();
	}

	@PUT
	@Path("forms/{id}")
	@ApiOperation(value = "Stores a blank json form")
	@Consumes(MediaType.TEXT_PLAIN)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Doc created") })
	public Response writeForm(String doc,
			@ApiParam(value = "A reference to the schema to be written", required = true) @PathParam("id") String id) {
		blankFormsPersistenceService.blatDoc(id, doc);
		log.info("Doc with id='" + id + "' received and persisted");

		return Response.status(Response.Status.OK).build();
	}

	@GET
	@Path("forms/{id}")
	@ApiOperation(value = "Retrieves a blank json form")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Doc retrieved") })
	public Response readForm(
			@ApiParam(value = "A reference to the schema to be read", required = true) @PathParam("id") String id) {
		return Response.status(Response.Status.OK).entity(blankFormsPersistenceService.readDoc(id)).build();
	}

	@GET
	@Path("forms")
	@ApiOperation(value = "Retrieves list of blank (starter) forms")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
	public Response listForms() {
		return Response.status(Response.Status.OK).entity(blankFormsPersistenceService.listDocs()).build();
	}

	@GET
	@Path("starter")
	@ApiOperation(value = "Provides a starter json schema to play")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Document retrieved") })
	public Response blank() throws Exception {
		JsonNode jsonNode = Utils.loadResource(SAMPLE_JSON_SCHEMA_FILENAME);
		return Response.status(200).entity(jsonNode.toString()).build();
	}

	private void initialisePersistenceService() {
		// initialising static in default constructor so exception of no DB can
		// be caught and REST IF still starts up
		// otherwise very cryptic exceptions thrown
		if (schemaPersistenceService == null) {
			try {
				schemaPersistenceService = GuiceBindingsModule.module.providePersistenceService("http",
						App.persistanceHostname, App.persistancePort, PersistenceService.SCHEMA_DB_NAME, "user",
						"password");
			} catch (Exception e) {
				// swallowing so that rest service becomes available
				log.error("Failed to initialise persistance service for '" + PersistenceService.SCHEMA_DB_NAME
						+ "' at " + App.persistanceHostname + ":" + App.persistancePort);
			}
		}
		if (metaSchemaPersistenceService == null) {
			try {
				metaSchemaPersistenceService = GuiceBindingsModule.module.providePersistenceService("http",
						App.persistanceHostname, App.persistancePort, PersistenceService.META_SCHEMA_DB_NAME, "user",
						"password");
			} catch (Exception e) {
				// swallowing so that rest service becomes available
				log.error("Failed to initialise persistance service for '" + PersistenceService.META_SCHEMA_DB_NAME
						+ "' at " + App.persistanceHostname + ":" + App.persistancePort);
			}
		}
		if (blankFormsPersistenceService == null) {
			try {
				blankFormsPersistenceService = GuiceBindingsModule.module.providePersistenceService("http",
						App.persistanceHostname, App.persistancePort, PersistenceService.BLANK_FORM_DB_NAME, "user",
						"password");
			} catch (Exception e) {
				// swallowing so that rest service becomes available
				log.error("Failed to initialise persistance service for '" + PersistenceService.BLANK_FORM_DB_NAME
						+ "' at " + App.persistanceHostname + ":" + App.persistancePort);
			}
		}
	}

}