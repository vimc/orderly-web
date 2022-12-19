package org.vaccineimpact.orderlyweb

import okhttp3.OkHttpClient
import org.vaccineimpact.orderlyweb.db.Config

class OutpackServerClient(
        config: Config,
        client: OkHttpClient = OkHttpClient()
) : PorcelainAPIClient("Outpack server", config["outpack.server"], client)
