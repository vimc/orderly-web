package org.vaccineimpact.reporting_api.tests.database_tests

import org.assertj.core.api.Assertions.assertThat
import org.jooq.impl.DSL.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.vaccineimpact.reporting_api.db.AppConfig
import org.vaccineimpact.reporting_api.db.JooqContext
import org.vaccineimpact.reporting_api.db.SQLiteTokenStore
import org.vaccineimpact.reporting_api.test_helpers.MontaguTests
import java.io.File

class SQLiteTokenStoreTests : MontaguTests()
{

    @Before
    fun createDatabase()
    {
        println("Looking for sqlite database at path: ${AppConfig()["onetime_token.db.template"]}")
        println("Working directory: ${System.getProperty("user.dir")}")

        val newDbFile = File(AppConfig()["onetime_token.db.location"])
        val source = File(AppConfig()["onetime_token.db.template"])

        println("Creating sqlite database at path: ${AppConfig()["onetime_token.db.location"]}")

        source.copyTo(newDbFile, true)
    }

    @After
    fun deleteDatabase()
    {

        println("Removing database at path: ${AppConfig()["onetime_token.db.location"]}")
        File(AppConfig()["onetime_token.db.location"]).delete()
    }

    private fun getJooqContext() = JooqContext(AppConfig()["onetime_token.db.location"])

    @Test
    fun `can create store`()
    {

        val sut = SQLiteTokenStore()
        sut.setup()
    }

    @Test
    fun `can insert new token`()
    {

        val sut = SQLiteTokenStore()
        sut.storeToken("testtoken")

        getJooqContext().use {

            val result = it.dsl.selectFrom(sut.ONETIME_TOKEN)
                    .where(sut.TOKEN.eq("testtoken"))
                    .fetchAny()

            assertThat(result).isNotNull()

        }
    }

    @Test
    fun `verifyToken returns true if token exists`()
    {


        val token = "testtoken"
        val sut = SQLiteTokenStore()

        getJooqContext().use {
            it.dsl.insertInto(sut.ONETIME_TOKEN)
                    .set(sut.TOKEN, token)
                    .execute()
        }

        val result = sut.validateOneTimeToken(token)

        assertThat(result).isTrue()
    }


    @Test
    fun `verifyToken deletes token after retrieval`()
    {


        val token = "testtoken"
        val sut = SQLiteTokenStore()

        getJooqContext().use {
            it.dsl.insertInto(table(name("ONETIME_TOKEN")))
                    .set(field(name("ONETIME_TOKEN.TOKEN")), token)
                    .execute()
        }

        sut.validateOneTimeToken(token)

        getJooqContext().use {
            val result = it.dsl.selectFrom(table(name("ONETIME_TOKEN")))
                    .where(field(name("ONETIME_TOKEN.TOKEN")).eq(token))
                    .fetchAny()

            assertThat(result).isNull()
        }
    }


    @Test
    fun `verifyToken returns false if token does not exist`()
    {


        val token = "testtoken"
        val sut = SQLiteTokenStore()

        val result = sut.validateOneTimeToken(token)

        assertThat(result).isFalse()
    }

}