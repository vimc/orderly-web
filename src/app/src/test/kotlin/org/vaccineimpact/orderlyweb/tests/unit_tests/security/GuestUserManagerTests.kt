
package org.vaccineimpact.orderlyweb.tests.unit_tests.security

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.pac4j.core.context.WebContext
import org.pac4j.core.context.session.SessionStore
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.profile.ProfileManager
import org.pac4j.sparkjava.SparkWebContext
import org.vaccineimpact.orderlyweb.db.repositories.AuthorizationRepository
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.security.OrderlyWebGuestUserManager
import org.vaccineimpact.orderlyweb.security.WebSecurityConfigFactory
import org.vaccineimpact.orderlyweb.security.authorization.orderlyWebPermissions
import org.vaccineimpact.orderlyweb.security.clients.GithubIndirectClient
import org.vaccineimpact.orderlyweb.security.clients.MontaguIndirectClient
import org.vaccineimpact.orderlyweb.security.clients.OrderlyWebIndirectClient
import spark.Request
import java.util.*
import javax.servlet.http.HttpServletRequest

class GuestUserManagerTests
{
    private val realUser = CommonProfile().apply { id = "some.real.user" }
    private val guestUser = CommonProfile().apply { id = "guest" }

    private val authRepo = mock<AuthorizationRepository> {
        on {getPermissionsForGroup("guest")} doReturn listOf(ReifiedPermission("reports.read", Scope.Global()))
    }
    
    class MockSessionStore: SessionStore
    {
        private val store: MutableMap<String, Any> = mutableMapOf()

        override fun getSessionId(context: WebContext?, createSession: Boolean): Optional<String>
        {
            return Optional.of("123")
        }

        override fun get(context: WebContext?, key: String?): Optional<Any>
        {
            return if (store.keys.contains(key)) Optional.of(store[key] as Any) else Optional.empty()
        }

        override fun set(context: WebContext?, key: String, value: Any)
        {
            store[key] = value
        }

        override fun destroySession(context: WebContext?): Boolean
        {
            TODO("Not yet implemented")
        }

        override fun getTrackableSession(context: WebContext?): Optional<Any>
        {
            TODO("Not yet implemented")
        }

        override fun buildFromTrackableSession(context: WebContext?, trackableSession: Any?): Optional<SessionStore>
        {
            TODO("Not yet implemented")
        }

        override fun renewSession(context: WebContext?): Boolean
        {
            TODO("Not yet implemented")
        }

    }

    private fun setUpMockSession(user: CommonProfile? = null): Pair<SparkWebContext, SessionStore>
    {
        val mockSession = FakeSession()
        val mockSessionStore = MockSessionStore()
        val mockRequest = mock<HttpServletRequest>() {
            on { session } doReturn mockSession
        }
        val mockSparkRequest = mock<Request> {
            on { raw() } doReturn mockRequest
        }
        val mockContext = mock<SparkWebContext> {
            on { sparkRequest } doReturn mockSparkRequest
        }
        if (user != null)
        {
            ProfileManager(mockContext, mockSessionStore).save(true, user, false)
        }
        return Pair(mockContext, mockSessionStore)
    }

    @Test
    fun `real user profiles are unchanged if using GithubIndirectClient`()
    {
        val (mockContext, mockSessionStore) = setUpMockSession(realUser)

        val config = WebSecurityConfigFactory(GithubIndirectClient("", ""), setOf()).build()
        val sut = OrderlyWebGuestUserManager()
        sut.updateProfile(true, mockContext, mockSessionStore, config, "GithubIndirectClient")

        val profile = ProfileManager(mockContext, mockSessionStore).profile
        assertThat(profile.get())
                .isEqualToComparingFieldByField(realUser)
    }

    @Test
    fun `real user profiles are unchanged if using MontaguIndirectClient`()
    {
        val (mockContext, mockSessionStore) = setUpMockSession(realUser)

        val config = WebSecurityConfigFactory(MontaguIndirectClient(), setOf()).build()
        val sut = OrderlyWebGuestUserManager()
        sut.updateProfile(true, mockContext, mockSessionStore, config, "MontaguIndirectClient")

        val profile = ProfileManager(mockContext, mockSessionStore).profile
        assertThat(profile.get())
                .isEqualToComparingFieldByField(realUser)
    }

    @Test
    fun `real user profiles are unchanged if using OrderlyWebIndirectClient`()
    {
        val (mockContext, mockSessionStore) = setUpMockSession(realUser)

        val config = WebSecurityConfigFactory(OrderlyWebIndirectClient(), setOf()).build()
        val sut = OrderlyWebGuestUserManager()
        sut.updateProfile(true, mockContext, mockSessionStore, config, "OrderlyWebIndirectClient")

        val profile = ProfileManager(mockContext, mockSessionStore).profile
        assertThat(profile.get())
                .isEqualToComparingFieldByField(realUser)
    }

    @Test
    fun `guest user profile is wiped if using GithubIndirectClient`()
    {
        val (mockContext, mockSessionStore) = setUpMockSession(guestUser)

        val config = WebSecurityConfigFactory(GithubIndirectClient("", ""), setOf()).build()
        val sut = OrderlyWebGuestUserManager()
        sut.updateProfile(true, mockContext, mockSessionStore, config, "GithubIndirectClient")

        val profile = ProfileManager(mockContext, mockSessionStore).profile
        assertThat(profile.isPresent)
                .isFalse()
    }

    @Test
    fun `guest user profile is wiped if guest user is not allowed`()
    {
        val (mockContext, mockSessionStore) = setUpMockSession(guestUser)

        val config = WebSecurityConfigFactory(OrderlyWebIndirectClient(), setOf()).build()
        val sut = OrderlyWebGuestUserManager()
        sut.updateProfile(false, mockContext, mockSessionStore, config, "OrderlyWebIndirectClient")

        val profile = ProfileManager(mockContext, mockSessionStore).profile
        assertThat(profile.isPresent)
                .isFalse()
    }

    @Test
    fun `guest user profile is wiped if using MontaguIndirectClient`()
    {
        val (mockContext, mockSessionStore) = setUpMockSession(guestUser)

        val config = WebSecurityConfigFactory(MontaguIndirectClient(), setOf()).build()
        val sut = OrderlyWebGuestUserManager()
        sut.updateProfile(true, mockContext, mockSessionStore, config, "MontaguIndirectClient")

        val profile = ProfileManager(mockContext, mockSessionStore).profile
        assertThat(profile.isPresent)
                .isFalse()
    }

    @Test
    fun `guest user profile is not added if using GithubIndirectClient`()
    {
        val (mockContext, mockSessionStore) = setUpMockSession()

        val config = WebSecurityConfigFactory(GithubIndirectClient("", ""), setOf()).build()
        val sut = OrderlyWebGuestUserManager()
        sut.updateProfile(true, mockContext, mockSessionStore, config, "GithubIndirectClient")

        val profile = ProfileManager(mockContext, mockSessionStore).profile
        assertThat(profile.isPresent)
                .isFalse()
    }

    @Test
    fun `guest user profile is not added if using MontaguIndirectClient`()
    {
        val (mockContext, mockSessionStore) = setUpMockSession()

        val config = WebSecurityConfigFactory(MontaguIndirectClient(), setOf()).build()
        val sut = OrderlyWebGuestUserManager()
        sut.updateProfile(true, mockContext, mockSessionStore, config, "MontaguIndirectClient")

        val profile = ProfileManager(mockContext, mockSessionStore).profile
        assertThat(profile.isPresent)
                .isFalse()
    }

    @Test
    fun `guest user profile is added if using OrderlyWebIndirectClient`()
    {
        val (mockContext, mockSessionStore) = setUpMockSession()

        val config = WebSecurityConfigFactory(OrderlyWebIndirectClient(), setOf()).build()
        val sut = OrderlyWebGuestUserManager(authRepo)
        sut.updateProfile(true, mockContext, mockSessionStore, config, "OrderlyWebIndirectClient")

        val profile = ProfileManager(mockContext, mockSessionStore).profile.get() as CommonProfile
        assertThat(profile.id).isEqualTo("guest")
        assertThat(profile.orderlyWebPermissions.count()).isEqualTo(1)
        assertThat(profile.orderlyWebPermissions.first().name).isEqualTo("reports.read")
    }

    @Test
    fun `existing guest user profile is updated if using OrderlyWebIndirectClient`()
    {
        val (mockContext, mockSessionStore) = setUpMockSession(guestUser)

        val config = WebSecurityConfigFactory(OrderlyWebIndirectClient(), setOf()).build()
        val sut = OrderlyWebGuestUserManager(authRepo)
        sut.updateProfile(true, mockContext, mockSessionStore, config, "OrderlyWebIndirectClient")

        val profile = ProfileManager(mockContext, mockSessionStore).profile.get() as CommonProfile
        assertThat(profile.id).isEqualTo("guest")
        assertThat(profile.orderlyWebPermissions.count()).isEqualTo(1)
        assertThat(profile.orderlyWebPermissions.first().name).isEqualTo("reports.read")
    }
}
