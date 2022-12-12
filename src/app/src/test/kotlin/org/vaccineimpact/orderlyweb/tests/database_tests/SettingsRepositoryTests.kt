package org.vaccineimpact.orderlyweb.tests.database_tests

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables
import org.vaccineimpact.orderlyweb.db.repositories.OrderlySettingsRepository
import org.vaccineimpact.orderlyweb.test_helpers.CleanDatabaseTests
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_SETTINGS

class SettingsRepositoryTests : CleanDatabaseTests()
{
    @BeforeEach
    fun addDefaultSettings()
    {
        //Settings table has been cleaned by base class
        JooqContext().use {
            it.dsl.insertInto(ORDERLYWEB_SETTINGS)
                    .set(ORDERLYWEB_SETTINGS.AUTH_ALLOW_GUEST, false)
                    .execute()
        }
    }

    @Test
    fun `can get auth allow guest`()
    {
        val sut = OrderlySettingsRepository()
        assertThat(sut.getAuthAllowGuest()).isFalse()

        JooqContext().use {
            it.dsl.update(Tables.ORDERLYWEB_SETTINGS)
                    .set(ORDERLYWEB_SETTINGS.AUTH_ALLOW_GUEST, true)
                    .execute()
        }

        assertThat(sut.getAuthAllowGuest()).isTrue()
    }

    @Test
    fun `can set auth allow guest`()
    {
        val sut = OrderlySettingsRepository()
        sut.setAuthAllowGuest(true)
        JooqContext().use {
            val result = it.dsl.select(ORDERLYWEB_SETTINGS.AUTH_ALLOW_GUEST)
                    .from(ORDERLYWEB_SETTINGS)
                    .fetchOne()

            assertThat(result[ORDERLYWEB_SETTINGS.AUTH_ALLOW_GUEST]).isTrue()
        }
    }
}
