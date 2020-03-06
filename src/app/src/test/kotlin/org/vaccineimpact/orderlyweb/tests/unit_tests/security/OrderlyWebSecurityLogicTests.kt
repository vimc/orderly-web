package org.vaccineimpact.orderlyweb.tests.unit_tests.security

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Test
import org.mockito.internal.verification.Times
import org.vaccineimpact.orderlyweb.security.AnonUserManager
import org.vaccineimpact.orderlyweb.security.OrderlyWebSecurityLogic
import org.vaccineimpact.orderlyweb.security.authentication.AuthenticationConfig
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests

class OrderlyWebSecurityLogicTests : TeamcityTests()
{
    @Test
    fun `anon user profile is managed if allow anon is true`()
    {
        val anonUserManager = mock<AnonUserManager>()
        val config = mock<AuthenticationConfig> {
            on { allowAnonUser } doReturn true
        }

        val sut = OrderlyWebSecurityLogic(config, anonUserManager)
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
        verify(anonUserManager, Times(1)).updateProfile(any(), any(), any())
    }

    @Test
    fun `anon user profile is not managed if allow anon is false`()
    {
        val anonUserManager = mock<AnonUserManager>()
        val config = mock<AuthenticationConfig> {
            on { allowAnonUser } doReturn false
        }

        val sut = OrderlyWebSecurityLogic(config, anonUserManager)
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
        verify(anonUserManager, Times(0)).updateProfile(any(), any(), any())
    }
}