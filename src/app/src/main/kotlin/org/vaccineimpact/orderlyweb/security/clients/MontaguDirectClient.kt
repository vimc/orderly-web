package org.vaccineimpact.orderlyweb.security.clients

import org.pac4j.core.client.DirectClient
import org.pac4j.core.context.HttpConstants
import org.pac4j.core.credentials.extractor.HeaderExtractor
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyUserRepository
import org.vaccineimpact.orderlyweb.models.ErrorInfo
import org.vaccineimpact.orderlyweb.security.authentication.MontaguAuthenticator
import org.vaccineimpact.orderlyweb.security.authorization.OrderlyAuthorizationGenerator
import org.vaccineimpact.orderlyweb.security.providers.RemoteHttpMontaguApiClient

class MontaguDirectClient : DirectClient(), OrderlyWebTokenCredentialClient
{
    override val errorInfo = ErrorInfo(
            "montagu-token-invalid",
            "Montagu token not supplied in Authorization header, or Montagu token was invalid"
    )

    override fun internalInit(forceReinit: Boolean)
    {
        defaultCredentialsExtractor(
                HeaderExtractor(
                        HttpConstants.AUTHORIZATION_HEADER,
                        "token "
                )
        )

        defaultAuthenticator(
                MontaguAuthenticator(
                        OrderlyUserRepository(),
                        RemoteHttpMontaguApiClient(AppConfig())
                )
        )
        setAuthorizationGenerator(OrderlyAuthorizationGenerator())
    }
}
