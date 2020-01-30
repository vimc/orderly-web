package org.vaccineimpact.orderlyweb.tests.unit_tests.templates

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.Test
import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.vaccineimpact.orderlyweb.viewmodels.DefaultViewModel
import org.vaccineimpact.orderlyweb.viewmodels.IndexViewModel

class DefaultViewModelTests : TeamcityTests()
{
    @Test
    fun `isAdmin is true if user can manage users`()
    {
        val mockContext = mock<ActionContext> {
            on { userProfile } doReturn CommonProfile().apply {
                id = "test.user"
            }
            on {
                hasPermission(ReifiedPermission("users.manage", Scope.Global()))
            } doReturn true
        }

        val sut = DefaultViewModel(mockContext, IndexViewModel.breadcrumb)
        assertThat(sut.isAdmin).isTrue()
    }

    @Test
    fun `isAdmin is false if user cannot manage users`()
    {
        val mockContext = mock<ActionContext> {
            on { userProfile } doReturn CommonProfile().apply {
                id = "test.user"
            }
            on {
                hasPermission(ReifiedPermission("users.manage", Scope.Global()))
            } doReturn false
        }

        val sut = DefaultViewModel(mockContext, IndexViewModel.breadcrumb)
        assertThat(sut.isAdmin).isFalse()
    }

    @Test
    fun `showPermissionManagement is false if isAdmin is false`()
    {
        val mockConfig = mock<Config>()
        {
            on { authorizationEnabled } doReturn true
        }

        val sut = DefaultViewModel(true,
                "username",
                isReviewer = true,
                isAdmin = false,
                breadcrumbs = listOf(IndexViewModel.breadcrumb),
                appConfig = mockConfig)

        assertThat(sut.showPermissionManagement).isFalse()
    }

    @Test
    fun `showPermissionManagement is false if auth is not enabled`()
    {
        val mockConfig = mock<Config>()
        {
            on { authorizationEnabled } doReturn false
        }

        val sut = DefaultViewModel(true,
                "username",
                isReviewer = true,
                isAdmin = true,
                breadcrumbs = listOf(IndexViewModel.breadcrumb),
                appConfig = mockConfig)

        assertThat(sut.showPermissionManagement).isFalse()
    }
}