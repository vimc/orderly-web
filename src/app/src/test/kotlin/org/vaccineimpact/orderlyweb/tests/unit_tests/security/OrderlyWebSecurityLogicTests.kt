package org.vaccineimpact.orderlyweb.tests.unit_tests.security

import com.nhaarman.mockito_kotlin.*
import org.junit.Test
import org.mockito.internal.verification.Times
import org.pac4j.sparkjava.SparkWebContext
import org.vaccineimpact.orderlyweb.security.GuestUserManager
import org.vaccineimpact.orderlyweb.security.OrderlyWebSecurityLogic
import org.vaccineimpact.orderlyweb.security.authentication.AuthenticationConfig

class OrderlyWebSecurityLogicTests
{
    @Test
    fun `guest user profile is updated with expected param if allow guest is true`()
    {
        val guestUserManager = mock<GuestUserManager>()
        val config = mock<AuthenticationConfig> {
            on { allowGuestUser } doReturn true
        }

        val sut = OrderlyWebSecurityLogic(config, guestUserManager)
        try
        {
            sut.perform(mock<SparkWebContext>(), mock(), mock(), mock(), mock(), "", "", "", false)
        }
        catch (e: Exception)
        {
            val t = e.message
            // will throw errors because of all the mocked params, but we don't care
            // because we're not testing that and the method we care about is called
            // before the exceptions get thrown
        }
        verify(guestUserManager).updateProfile(eq(true), any(), any(), any(), any())
    }

    @Test
    fun `guest user profile is updated with expected param if allow guest is false`()
    {
        val guestUserManager = mock<GuestUserManager>()
        val config = mock<AuthenticationConfig> {
            on { allowGuestUser } doReturn false
        }

        val sut = OrderlyWebSecurityLogic(config, guestUserManager)
        try
        {
            sut.perform(mock<SparkWebContext>(), mock(), mock(), mock(), mock(), "", "", "", false)
        }
        catch (e: Exception)
        {
            // will throw errors because of all the mocked params, but we don't care
            // because we're not testing that and the method we care about is called
            // before the exceptions get thrown
        }
        verify(guestUserManager).updateProfile(eq(false), any(), any(), any(), any())
    }
}