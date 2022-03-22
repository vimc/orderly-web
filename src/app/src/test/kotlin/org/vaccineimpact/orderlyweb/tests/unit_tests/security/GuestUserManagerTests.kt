package org.vaccineimpact.orderlyweb.tests.unit_tests.security

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.pac4j.core.context.session.J2ESessionStore
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
import javax.servlet.http.HttpServletRequest

class GuestUserManagerTests
{
    private val realUser = CommonProfile().apply { id = "some.real.user" }
    private val guestUser = CommonProfile().apply { id = "guest" }

    private val authRepo = mock<AuthorizationRepository> {
        on { getPermissionsForGroup("guest") } doReturn listOf(ReifiedPermission("reports.read", Scope.Global()))
    }

    private fun setUpMockSession(user: CommonProfile? = null): SparkWebContext
    {
        val mockSession = FakeSession()
        val mockSessionStore = J2ESessionStore()
        val mockRequest = mock<HttpServletRequest>() {
            on { session } doReturn mockSession
        }
        val mockContext = mock<SparkWebContext> {
            on { sessionStore } doReturn mockSessionStore
            on { request } doReturn mockRequest
        }
        if (user != null)
        {
            ProfileManager<CommonProfile>(mockContext).save(true, user, false)
        }
        return mockContext
    }

    @Test
    fun `real user profiles are unchanged if using GithubIndirectClient`()
    {
        val mockContext = setUpMockSession(realUser)

        val config = WebSecurityConfigFactory(GithubIndirectClient("", ""), setOf()).build()
        val sut = OrderlyWebGuestUserManager()
        sut.updateProfile(true, mockContext, config, "GithubIndirectClient")

        val profile = ProfileManager<CommonProfile>(mockContext).get(true)
        assertThat(profile.get())
                .isEqualToComparingFieldByField(realUser)
    }

    @Test
    fun `real user profiles are unchanged if using MontaguIndirectClient`()
    {
        val mockContext = setUpMockSession(realUser)

        val config = WebSecurityConfigFactory(MontaguIndirectClient(), setOf()).build()
        val sut = OrderlyWebGuestUserManager()
        sut.updateProfile(true, mockContext, config, "MontaguIndirectClient")

        val profile = ProfileManager<CommonProfile>(mockContext).get(true)
        assertThat(profile.get())
                .isEqualToComparingFieldByField(realUser)
    }

    @Test
    fun `real user profiles are unchanged if using OrderlyWebIndirectClient`()
    {
        val mockContext = setUpMockSession(realUser)

        val config = WebSecurityConfigFactory(OrderlyWebIndirectClient(), setOf()).build()
        val sut = OrderlyWebGuestUserManager()
        sut.updateProfile(true, mockContext, config, "OrderlyWebIndirectClient")

        val profile = ProfileManager<CommonProfile>(mockContext).get(true)
        assertThat(profile.get())
                .isEqualToComparingFieldByField(realUser)
    }

    @Test
    fun `guest user profile is wiped if using GithubIndirectClient`()
    {
        val mockContext = setUpMockSession(guestUser)

        val config = WebSecurityConfigFactory(GithubIndirectClient("", ""), setOf()).build()
        val sut = OrderlyWebGuestUserManager()
        sut.updateProfile(true, mockContext, config, "GithubIndirectClient")

        val profile = ProfileManager<CommonProfile>(mockContext).get(true)
        assertThat(profile.isPresent)
                .isFalse()
    }

    @Test
    fun `guest user profile is wiped if guest user is not allowed`()
    {
        val mockContext = setUpMockSession(guestUser)

        val config = WebSecurityConfigFactory(OrderlyWebIndirectClient(), setOf()).build()
        val sut = OrderlyWebGuestUserManager()
        sut.updateProfile(false, mockContext, config, "OrderlyWebIndirectClient")

        val profile = ProfileManager<CommonProfile>(mockContext).get(true)
        assertThat(profile.isPresent)
                .isFalse()
    }

    @Test
    fun `guest user profile is wiped if using MontaguIndirectClient`()
    {
        val mockContext = setUpMockSession(guestUser)

        val config = WebSecurityConfigFactory(MontaguIndirectClient(), setOf()).build()
        val sut = OrderlyWebGuestUserManager()
        sut.updateProfile(true, mockContext, config, "MontaguIndirectClient")

        val profile = ProfileManager<CommonProfile>(mockContext).get(true)
        assertThat(profile.isPresent)
                .isFalse()
    }

    @Test
    fun `guest user profile is not added if using GithubIndirectClient`()
    {
        val mockContext = setUpMockSession()

        val config = WebSecurityConfigFactory(GithubIndirectClient("", ""), setOf()).build()
        val sut = OrderlyWebGuestUserManager()
        sut.updateProfile(true, mockContext, config, "GithubIndirectClient")

        val profile = ProfileManager<CommonProfile>(mockContext).get(true)
        assertThat(profile.isPresent)
                .isFalse()
    }

    @Test
    fun `guest user profile is not added if using MontaguIndirectClient`()
    {
        val mockContext = setUpMockSession()

        val config = WebSecurityConfigFactory(MontaguIndirectClient(), setOf()).build()
        val sut = OrderlyWebGuestUserManager()
        sut.updateProfile(true, mockContext, config, "MontaguIndirectClient")

        val profile = ProfileManager<CommonProfile>(mockContext).get(true)
        assertThat(profile.isPresent)
                .isFalse()
    }

    @Test
    fun `guest user profile is added if using OrderlyWebIndirectClient`()
    {
        val mockContext = setUpMockSession()

        val config = WebSecurityConfigFactory(OrderlyWebIndirectClient(), setOf()).build()
        val sut = OrderlyWebGuestUserManager(authRepo)
        sut.updateProfile(true, mockContext, config, "OrderlyWebIndirectClient")

        val profile = ProfileManager<CommonProfile>(mockContext).get(true).get()
        assertThat(profile.id).isEqualTo("guest")
        assertThat(profile.orderlyWebPermissions.count()).isEqualTo(1)
        assertThat(profile.orderlyWebPermissions.first().name).isEqualTo("reports.read")
    }

    @Test
    fun `existing guest user profile is updated if using OrderlyWebIndirectClient`()
    {
        val mockContext = setUpMockSession(guestUser)

        val config = WebSecurityConfigFactory(OrderlyWebIndirectClient(), setOf()).build()
        val sut = OrderlyWebGuestUserManager(authRepo)
        sut.updateProfile(true, mockContext, config, "OrderlyWebIndirectClient")

        val profile = ProfileManager<CommonProfile>(mockContext).get(true).get()
        assertThat(profile.id).isEqualTo("guest")
        assertThat(profile.orderlyWebPermissions.count()).isEqualTo(1)
        assertThat(profile.orderlyWebPermissions.first().name).isEqualTo("reports.read")
    }
}
