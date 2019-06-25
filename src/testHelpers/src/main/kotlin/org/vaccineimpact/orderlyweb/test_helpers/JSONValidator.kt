package org.vaccineimpact.orderlyweb.test_helpers

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonNode
import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.core.load.configuration.LoadingConfiguration
import com.github.fge.jsonschema.core.load.uri.URITranslatorConfiguration
import com.github.fge.jsonschema.main.JsonSchemaFactory
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail

class JSONValidator
{
    private val schemaFactory = makeSchemaFactory()
    private val responseSchema = readSchema("Response")

    fun getData(response: String): JsonNode
    {
        val json = parseJson(response)
        return json["data"]
    }

    fun validateAgainstSchema(response: String, schemaName: String)
    {
        val json = parseJson(response)
        // Everything must meet the basic response schema
        checkResultSchema(json, "success")
        // Then use the more specific schema on the data portion
        val data = json["data"]
        val schema = readSchema(schemaName)
        assertValidates(schema, data)
    }

    fun validateError(response: String,
                      expectedErrorCode: String?,
                      expectedErrorText: String?)
    {
        val json = parseJson(response)
        checkResultSchema(json, "failure")
        if (expectedErrorCode != null)
        {
            val error = json["errors"].singleOrNull { it["code"].asText() == expectedErrorCode }
            if (error != null)
            {
                assertThat(error["message"].asText()).contains(expectedErrorText)
            }
            else
            {
                fail("Expected error code '$expectedErrorCode' to be present in $response")
            }
        }
    }

    fun validateMultipleAuthErrors(response: String)
    {
        validateError(response,
                expectedErrorCode = "bearer-token-invalid",
                expectedErrorText = "Bearer token not supplied in Authorization header, or bearer token was invalid")
        validateError(response,
                expectedErrorCode = "onetime-token-invalid",
                expectedErrorText = "Onetime token not supplied, or onetime token was invalid")
    }

    private fun checkResultSchema(json: JsonNode, expectedStatus: String)
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