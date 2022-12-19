package org.vaccineimpact.orderlyweb

import okhttp3.OkHttpClient
import org.vaccineimpact.orderlyweb.db.Config

class OutpackServer(
        config: Config,
        client: OkHttpClient = OkHttpClient()
) : PorcelainAPIServer("Outpack server", config["outpack.server"], client)
