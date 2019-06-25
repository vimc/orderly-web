package org.vaccineimpact.orderlyweb.test_helpers

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonNode
import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.main.JsonSchemaFactory
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import java.io.File

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
        assertValidates("Response", json)
        val status = json["status"].textValue()
        Assertions.assertThat(status)
                .isEqualTo(expectedStatus)
    }

    private fun assertValidates(name: String, json: JsonNode)
    {
        val file = File("../../docs/spec/$name.schema.json").toURI()
        val report = schemaFactory.getJsonSchema(file.toString()).validate(json)
        if (!report.isSuccess)
        {
            Assertions.fail("JSON failed schema validation. Attempted to validate: $json against $name. Report follows: $report")
        }
    }

    private fun makeSchemaFactory(): JsonSchemaFactory
    {
        return JsonSchemaFactory.byDefault()
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