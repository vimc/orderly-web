package org.vaccineimpact.reporting_api.tests.integration_tests.validators

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonNode
import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.core.load.configuration.LoadingConfiguration
import com.github.fge.jsonschema.core.load.uri.URITranslatorConfiguration
import com.github.fge.jsonschema.main.JsonSchemaFactory
import org.assertj.core.api.Assertions

class JSONValidator : Validator
{
    private val schemaFactory = makeSchemaFactory()
    private val responseSchema = readSchema("Response")

    fun validateAgainstSchema(response: String, schemaName: String)
    {
        val json = parseJson(response)
        // Everything must meet the basic response schema
        checkResultSchema(json, response, "success")
        // Then use the more specific schema on the data portion
        val data = json["data"]
        val schema = readSchema(schemaName)
        assertValidates(schema, data)
    }

    override fun validateError(response: String,
                               expectedErrorCode: String?,
                               expectedErrorText: String?,
                               assertionText: String?)
    {
        val json = parseJson(response)
        checkResultSchema(json, response, "failure")
        val error = json["errors"].first()
        if (expectedErrorCode != null)
        {
            Assertions.assertThat(error["code"].asText())
                    .withFailMessage("Expected error code to be '$expectedErrorCode' in $response")
                    .isEqualTo(expectedErrorCode)
        }
        if (expectedErrorText != null)
        {
            Assertions.assertThat(error["message"].asText()).contains(expectedErrorText)
        }
    }
    override fun validateSuccess(response: String)
    {
        val json = parseJson(response)
        checkResultSchema(json, response, "success")
    }

    private fun checkResultSchema(json: JsonNode, jsonAsString: String, expectedStatus: String)
    {
        assertValidates(responseSchema, json)
        val status = json["status"].textValue()
        Assertions.assertThat(status)
                .isEqualTo(expectedStatus)
    }

    private fun readSchema(name: String): JsonNode = JsonLoader.fromResource("/spec/$name.schema.json")

    private fun assertValidates(schema: JsonNode, json: JsonNode)
    {
        val report = schemaFactory.getJsonSchema(schema).validate(json)
        if (!report.isSuccess)
        {
            Assertions.fail("JSON failed schema validation. Attempted to validate: $json against $schema. Report follows: $report")
        }
    }

    private fun makeSchemaFactory(): JsonSchemaFactory
    {
        val namespace = "resource:/spec/"
        val uriTranslatorConfig = URITranslatorConfiguration
                .newBuilder()
                .setNamespace(namespace)
                .freeze()
        val loadingConfig = LoadingConfiguration.newBuilder()
                .setURITranslatorConfiguration(uriTranslatorConfig)
                .freeze()
        return JsonSchemaFactory.newBuilder()
                .setLoadingConfiguration(loadingConfig)
                .freeze()
    }

    private fun parseJson(jsonAsString: String): JsonNode
    {
        return try
        {
            JsonLoader.fromString(jsonAsString)
        }
        catch (e: JsonParseException)
        {
            throw Exception("Failed to parse text as JSON.\nText was: $jsonAsString\n\n$e")
        }
    }
}