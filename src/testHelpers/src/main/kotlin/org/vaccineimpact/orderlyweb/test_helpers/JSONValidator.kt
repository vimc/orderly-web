package org.vaccineimpact.orderlyweb.test_helpers

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonNode
import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.core.load.Dereferencing
import com.github.fge.jsonschema.core.load.configuration.LoadingConfiguration
import com.github.fge.jsonschema.main.JsonSchemaFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import java.io.File
import java.net.URI

class JSONValidator
{
    private val schemaFactory = makeSchemaFactory()

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
        assertValidates(schemaName, data)
    }

    fun validateAgainstOrderlySchema(response: String, schemaName: String)
    {
        val json = parseJson(response)
        // Everything must meet the basic response schema
        checkResultSchema(json, "success")
        // Then use the more specific schema on the data portion
        val data = json["data"]
        assertValidates(schemaName, data, true)
    }

    fun validateError(response: String,
                      expectedError: String?,
                      expectedErrorText: String?)
    {
        val json = parseJson(response)
        checkResultSchema(json, "failure")
        if (expectedError != null)
        {
            val error = json["errors"].singleOrNull { it["error"].asText() == expectedError }
            if (error != null)
            {
                assertThat(error["detail"].asText()).contains(expectedErrorText)
            }
            else
            {
                fail("Expected error '$expectedError' to be present in $response")
            }
        }
    }

    fun validateMultipleAuthErrors(response: String)
    {
        validateError(response,
                expectedError = "bearer-token-invalid",
                expectedErrorText = "Bearer token not supplied in Authorization header, or bearer token was invalid")
    }

    private fun checkResultSchema(json: JsonNode, expectedStatus: String)
    {
        assertValidates("Response", json)
        val status = json["status"].textValue()
        assertThat(status)
                .isEqualTo(expectedStatus)
    }

    private fun assertValidates(name: String, json: JsonNode, orderlySchema: Boolean = false)
    {
        val uri = if (orderlySchema){
            URI("https://raw.githubusercontent.com/vimc/orderly.server/master/inst/schema/$name.schema.json")
        }
        else {
            File("../../docs/spec/$name.schema.json").toURI()
        }
        val report = schemaFactory.getJsonSchema(uri.toString()).validate(json)
        if (!report.isSuccess)
        {
            fail("JSON failed schema validation. Attempted to validate: $json against $name. Report follows: $report")
        }
    }

    private fun makeSchemaFactory(): JsonSchemaFactory
    {
        val loadingConfig = LoadingConfiguration.newBuilder()
                .dereferencing(Dereferencing.INLINE)
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
