package org.vaccineimpact.orderlyweb.tests.database_tests

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.IndexController
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.PermissionSet
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests

class OrderlyTests : TeamcityTests()
{
    @Test
    fun `initialises Orderly correctly when user is reviewer`()
    {
        val mockContext = mock<ActionContext> {
            on { this.hasPermission(ReifiedPermission("reports.review", Scope.Global())) } doReturn true
        }

        val sut = Orderly(mockContext)
        assertThat(sut.isReviewer).isTrue()
    }

    @Test
    fun `initialises Orderly correctly when user is not reviewer`()
    {
        val mockContext = mock<ActionContext> {
            on { this.hasPermission(ReifiedPermission("reports.review", Scope.Global())) } doReturn false
        }

        val sut = Orderly(mockContext)
        assertThat(sut.isReviewer).isFalse()
    }

    @Test
    fun `initialises Orderly correctly when user is global reader`()
    {
        val mockContext = mock<ActionContext> {
            on { this.hasPermission(ReifiedPermission("reports.read", Scope.Global())) } doReturn true
        }

        val sut = Orderly(mockContext)
        assertThat(sut.isGlobalReader).isTrue()
    }

    @Test
    fun `initialises Orderly correctly when user is not global reader`()
    {
        val mockContext = mock<ActionContext> {
            on { this.hasPermission(ReifiedPermission("reports.read", Scope.Global())) } doReturn false
        }

        val sut = Orderly(mockContext)
        assertThat(sut.isGlobalReader).isFalse()
    }


    @Test
    fun `passes through names of reports user can read`()
    {
        val mockContext = mock<ActionContext> {
            on { reportReadingScopes } doReturn listOf("minimal")
        }

        val sut = Orderly(mockContext)
        assertThat(sut.reportReadingScopes).hasSameElementsAs(listOf("minimal"))
    }
}