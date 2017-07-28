package org.vaccineimpact.reporting_api.db

import org.vaccineimpact.reporting_api.security.OnetimeTokenStore

object TokenStore
{
    val instance: OnetimeTokenStore = SQLiteTokenStore()
}
