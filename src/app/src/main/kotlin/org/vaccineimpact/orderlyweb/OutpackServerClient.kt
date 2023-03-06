package org.vaccineimpact.orderlyweb

import okhttp3.OkHttpClient
import org.vaccineimpact.orderlyweb.app_start.OrderlyWeb.Companion.httpClient
import org.vaccineimpact.orderlyweb.db.Config

class OutpackServerClient(
        config: Config,
        client: OkHttpClient = httpClient
) : PorcelainAPIClient("Outpack server", config["outpack.server"], client)
