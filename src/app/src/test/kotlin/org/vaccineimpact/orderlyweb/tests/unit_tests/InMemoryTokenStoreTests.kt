package org.vaccineimpact.orderlyweb.tests.unit_tests

import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.InMemoryTokenStore

class InMemoryTokenStoreTests
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