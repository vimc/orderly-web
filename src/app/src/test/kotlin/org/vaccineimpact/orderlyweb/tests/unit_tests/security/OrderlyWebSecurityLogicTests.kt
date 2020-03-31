package org.vaccineimpact.orderlyweb.tests.unit_tests.security

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Test
import org.mockito.internal.verification.Times
import org.vaccineimpact.orderlyweb.security.GuestUserManager
import org.vaccineimpact.orderlyweb.security.OrderlyWebSecurityLogic
import org.vaccineimpact.orderlyweb.security.authentication.AuthenticationConfig
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests

class OrderlyWebSecurityLogicTests : TeamcityTests()
{
    @Test
    fun `guest user profile is managed if allow guest is true`()
    {
        val guestUserManager = mock<GuestUserManager>()
        val config = mock<AuthenticationConfig> {
            on { allowGuestUser } doReturn true
        }

        val sut = OrderlyWebSecurityLogic(config, guestUserManager)
        try
        {
            sut.perform(mock(), mock(), mock(), mock(), "", "", "", false)
        }
        catch (e: Exception)
        {
            // will throw errors because of all the mocked params, but we don't care
            // because we're not testing that and the method we care about is called
            // before the exceptions get thrown
        }
        verify(guestUserManager, Times(1)).updateProfile(any(), any(), any())
    }

    @Test
    fun `guest user profile is not managed if allow guest is false`()
    {
        val guestUserManager = mock<GuestUserManager>()
        val config = mock<AuthenticationConfig> {
            on { allowGuestUser } doReturn false
        }

        val sut = OrderlyWebSecurityLogic(config, guestUserManager)
        try
        {
            sut.perform(mock(), mock(), mock(), mock(), "", "", "", false)
        }
        catch (e: Exception)
        {
            // will throw errors because of all the mocked params, but we don't care
            // because we're not testing that and the method we care about is called
            // before the exceptions get thrown
        }
        verify(guestUserManager, Times(0)).updateProfile(any(), any(), any())
    }
}