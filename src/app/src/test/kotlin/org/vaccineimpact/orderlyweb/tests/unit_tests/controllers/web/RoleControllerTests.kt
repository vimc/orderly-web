package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web

import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.RoleController
import org.vaccineimpact.orderlyweb.db.AuthorizationRepository
import org.vaccineimpact.orderlyweb.db.RoleRepository
import org.vaccineimpact.orderlyweb.models.User
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.models.permissions.Role
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.vaccineimpact.orderlyweb.viewmodels.RoleViewModel

class RoleControllerTests : TeamcityTests()
{
    private val singleRoleFromRepo = listOf(Role("Funders",
            listOf(User("test.user", "Test User", "test@example.com"),
                    User("unknown", "unknown", "funder@example.com"),
                    User("funder.user", "unknown", "another@example.com")
            )))

    private val multipleRolesFromRepo = listOf(
        Role("Science", listOf()),
        Role("Funders", listOf()),
        Role("Tech", listOf())
    )

    private val roleForAlphabeticFromRepo = listOf(Role("Science", listOf(
            User("c.user", "C User", "testc@example.com"),
            User("a.user", "A User", "test@example.com"),
            User("b.user", "B User", "testb@example.com")
        )))

    private fun assertExpectedSingleRoleViewModel(result: List<RoleViewModel>)
    {
        assertThat(result.count()).isEqualTo(1)
        assertThat(result[0].name).isEqualTo("Funders")

        val members = result[0].members
        assertThat(members[0].displayName).isEqualTo("Test User")
        assertThat(members[1].displayName).isEqualTo("funder.user")
        assertThat(members[2].displayName).isEqualTo("funder@example.com")
        assertThat(members[0].email).isEqualTo("test@example.com")
        assertThat(members[1].email).isEqualTo("another@example.com")
        assertThat(members[2].email).isEqualTo("funder@example.com")
    }

    private fun assertExpectedMultipleRoleViewModels(result: List<RoleViewModel>)
    {
        assertThat(result[0].name).isEqualTo("Funders")
        assertThat(result[1].name).isEqualTo("Science")
        assertThat(result[2].name).isEqualTo("Tech")
    }

    private fun assertExpectedAlphabeticRoleMembers(result: List<RoleViewModel>)
    {
        val members = result[0].members
        assertThat(members.map { it.username }).containsExactly("a.user", "b.user", "c.user")
    }

    @Test
    fun `getGlobalReportReaderGroups builds user group view models`()
    {
        val repo = mock<RoleRepository> {
            on { getGlobalReportReaderRoles() } doReturn singleRoleFromRepo
        }

        val sut = RoleController(mock(), repo)
        val result = sut.getGlobalReportReaders()
        assertExpectedSingleRoleViewModel(result)
    }

    @Test
    fun `getAllRoles builds user group view models`()
    {
        val repo = mock<RoleRepository> {
            on { getAllRoles() } doReturn singleRoleFromRepo
        }

        val sut = RoleController(mock(), repo)
        val result = sut.getAll()
        assertExpectedSingleRoleViewModel(result)
    }

    @Test
    fun `getGlobalReportReaderGroups orders user group view models alphabetically`()
    {
        val repo = mock<RoleRepository> {
            on { getGlobalReportReaderRoles() } doReturn multipleRolesFromRepo
        }

        val sut = RoleController(mock(), repo)
        val result = sut.getGlobalReportReaders()
        assertExpectedMultipleRoleViewModels(result)
    }

    @Test
    fun `getAllRoles orders user group view models alphabetically`()
    {
        val repo = mock<RoleRepository> {
            on { getAllRoles() } doReturn multipleRolesFromRepo
        }

        val sut = RoleController(mock(), repo)
        val result = sut.getAll()
        assertExpectedMultipleRoleViewModels(result)
    }

    @Test
    fun `getGlobalReportReaderGroups orders user group view model members alphabetically`()
    {
        val repo = mock<RoleRepository> {
            on { getGlobalReportReaderRoles() } doReturn roleForAlphabeticFromRepo
        }

        val sut = RoleController(mock(), repo)
        val result = sut.getGlobalReportReaders()
        assertExpectedAlphabeticRoleMembers(result)
    }

    @Test
    fun `getAllRoles orders user group view model members alphabetically`()
    {
        val repo = mock<RoleRepository> {
            on { getAllRoles() } doReturn roleForAlphabeticFromRepo
        }

        val sut = RoleController(mock(), repo)
        val result = sut.getAll()
        assertExpectedAlphabeticRoleMembers(result)
    }

    private val actionContextWithReport = mock<ActionContext> {
        on { this.params(":report") } doReturn "r1"
    }

    @Test
    fun `getScopedReportReaders builds user group view models`()
    {
        val repo = mock<RoleRepository> {
            on { getScopedReportReaderRoles("r1") } doReturn listOf(Role("Funders",
                    listOf(User("test.user", "Test User", "test@example.com"),
                            User("unknown", "unknown", "funder@example.com"),
                            User("funder.user", "unknown", "another@example.com")
                    )))
        }

        val sut = RoleController(actionContextWithReport, repo)
        val result = sut.getScopedReportReaders()
        assertThat(result.count()).isEqualTo(1)
        assertThat(result[0].name).isEqualTo("Funders")

        val members = result[0].members
        assertThat(members[0].displayName).isEqualTo("Test User")
        assertThat(members[1].displayName).isEqualTo("funder.user")
        assertThat(members[2].displayName).isEqualTo("funder@example.com")
        assertThat(members[0].email).isEqualTo("test@example.com")
        assertThat(members[1].email).isEqualTo("another@example.com")
        assertThat(members[2].email).isEqualTo("funder@example.com")
    }

    @Test
    fun `getScopedReportReaders orders user group view models alphabetically`()
    {
        val repo = mock<RoleRepository> {
            on { getScopedReportReaderRoles("r1") } doReturn multipleRolesFromRepo
        }

        val sut = RoleController(actionContextWithReport, repo)
        val result = sut.getScopedReportReaders()
        assertExpectedMultipleRoleViewModels(result)
    }

    @Test
    fun `getScopedReportReaders orders user group view model members alphabetically`()
    {
        val repo = mock<RoleRepository> {
            on { getScopedReportReaderRoles("r1") } doReturn roleForAlphabeticFromRepo
        }

        val sut = RoleController(actionContextWithReport,repo)
        val result = sut.getScopedReportReaders()
        assertExpectedAlphabeticRoleMembers(result)
    }

    @Test
    fun `can get role names`()
    {
        val repo = mock<RoleRepository> {
            on { getAllRoleNames() } doReturn listOf("Science", "Funders")
        }
        val sut = RoleController(mock(), repo)
        assertThat(sut.getAllRoleNames()).containsExactly("Science", "Funders")
    }
}