package org.vaccineimpact.reporting_api.tests.integration_tests.helpers

import khttp.responses.Response
import org.jooq.impl.DSL.*
import org.vaccineimpact.api.models.Scope
import org.vaccineimpact.api.models.permissions.ReifiedPermission
import org.vaccineimpact.api.models.permissions.ReifiedRole
import org.vaccineimpact.reporting_api.ContentTypes
import org.vaccineimpact.reporting_api.db.Config
import org.vaccineimpact.reporting_api.db.JooqContext
import org.vaccineimpact.reporting_api.security.MontaguUser
import org.vaccineimpact.reporting_api.security.WebTokenHelper
import org.vaccineimpact.reporting_api.tests.integration_tests.APITests

class RequestHelper
{
    init
    {
        CertificateHelper.disableCertificateValidation()
    }

    private val baseUrl: String = "http://localhost:${Config["app.port"]}/v1"

    val fakeUser = MontaguUser("tettusername", "user", "*/reports.read")

    fun get(url: String, contentType: String = ContentTypes.json): Response
    {
        var headers = mapOf(
                "Accept" to contentType,
                "Accept-Encoding" to "gzip"
        )

        val token = APITests.tokenHelper
                .generateToken(fakeUser)

        headers += mapOf("Authorization" to "Bearer $token")

        return get(baseUrl + url, headers)
    }

    fun generateOnetimeToken(url: String): String
    {
        val token = WebTokenHelper.oneTimeTokenHelper.issuer
                .generateOneTimeActionToken(fakeUser, "/v1$url")

        JooqContext(Config["onetime_token.db.location"]).use {

            it.dsl.insertInto(table(name("ONETIME_TOKEN")))
                    .set(field(name("ONETIME_TOKEN.TOKEN")), token)
                    .execute()
        }

        return token
    }

    fun getWrongAuth(url: String, contentType: String = ContentTypes.json): Response
    {
        var headers = mapOf(
                "Accept" to contentType,
                "Accept-Encoding" to "gzip"
        )

        val token = "faketoken"
        headers += mapOf("Authorization" to "Bearer $token")

        return get(baseUrl + url, headers)
    }

    fun getWrongPermissions(url: String, contentType: String = ContentTypes.json): Response
    {
        var headers = mapOf(
                "Accept" to contentType,
                "Accept-Encoding" to "gzip"
        )

        val token = APITests.tokenHelper.generateToken(MontaguUser("tettusername", "user", "*/fake-perm"))

        headers += mapOf("Authorization" to "Bearer $token")

        return get(baseUrl + url, headers)
    }

    fun getNoAuth(url: String, contentType: String = ContentTypes.json): Response
    {
        val headers = mapOf(
                "Accept" to contentType,
                "Accept-Encoding" to "gzip"
        )

        return get(baseUrl + url, headers)
    }

    private fun get(url: String, headers: Map<String, String>)
            = khttp.get(url, headers)
}