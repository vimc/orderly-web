package org.vaccineimpact.reporting_api.tests.unit_tests

import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.reporting_api.db.InMemoryTokenStore
import org.vaccineimpact.reporting_api.test_helpers.MontaguTests

class InMemoryTokenStoreTests : MontaguTests()
{
    val token = "Cupcakes"
    val badToken = "Biscuits"

    @Test
    fun `validateOneTimeToken returns false for new store`()
    {
        val store = InMemoryTokenStore()
        Assertions.assertThat(store.validateOneTimeToken(token)).isFalse()
    }

    @Test
    fun `validateOneTimeToken returns true if token exists`()
    {
        val store = InMemoryTokenStore()
        store.storeToken(token)
        Assertions.assertThat(store.validateOneTimeToken(token)).isTrue()
    }

    @Test
    fun `validateOneTimeToken does not confuse tokens`()
    {
        val store = InMemoryTokenStore()
        store.storeToken(token)
        Assertions.assertThat(store.validateOneTimeToken(badToken)).isFalse()
    }

    @Test
    fun `validateOneTimeToken deletes token after retrieval`()
    {
        val store = InMemoryTokenStore()
        store.storeToken(token)
        store.validateOneTimeToken(token)
        Assertions.assertThat(store.validateOneTimeToken(token)).isFalse()
    }
}