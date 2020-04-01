package org.vaccineimpact.orderlyweb.db.repositories

import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_SETTINGS

interface SettingsRepository
{
    fun getAuthAllowGuest(): Boolean;
    fun setAuthAllowGuest(value: Boolean);
}

class OrderlySettingsRepository: SettingsRepository
{
    override fun getAuthAllowGuest(): Boolean
    {
        return JooqContext().use {
            it.dsl.select(ORDERLYWEB_SETTINGS.AUTH_ALLOW_GUEST)
                    .from(ORDERLYWEB_SETTINGS)
                    .fetchOne()[ORDERLYWEB_SETTINGS.AUTH_ALLOW_GUEST]
        }
    }

    override fun setAuthAllowGuest(value: Boolean)
    {
        JooqContext().use {
            it.dsl.update(ORDERLYWEB_SETTINGS)
                    .set(ORDERLYWEB_SETTINGS.AUTH_ALLOW_GUEST, value)
                    .execute()
        }
    }

}