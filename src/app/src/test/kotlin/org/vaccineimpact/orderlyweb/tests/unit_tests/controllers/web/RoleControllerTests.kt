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
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.User
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.models.permissions.Role
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests

class RoleControllerTests : TeamcityTests()
{
    @Test
    fun `getGlobalReportReaderGroups builds user group view models`()
    {
        val repo = mock<RoleRepository> {
            on { getGlobalReportReaderRoles() } doReturn listOf(Role("Funders",
                    listOf(User("test.user", "Test User", "test@example.com"),
                            User("unknown", "unknown", "funder@example.com"),
                            User("funder.user", "unknown", "another@example.com")
                    ), listOf(ReifiedPermission("reports.read", Scope.Global()),
                                ReifiedPermission("reports.review", Scope.Specific("report", "r1")))))
        }

        val sut = RoleController(mock(), repo)
        val result = sut.getGlobalReportReaders()
        assertThat(result.count()).isEqualTo(1)
        assertThat(result[0].name).isEqualTo("Funders")

        val members = result[0].members
        assertThat(members[0].displayName).isEqualTo("Test User")
        assertThat(members[1].displayName).isEqualTo("funder.user")
        assertThat(members[2].displayName).isEqualTo("funder@example.com")
        assertThat(members[0].email).isEqualTo("test@example.com")
        assertThat(members[1].email).isEqualTo("another@example.com")
        assertThat(members[2].email).isEqualTo("funder@example.com")

        val perms = result[0].permissions
        assertThat(perms[0].name).isEqualTo("reports.read")
        assertThat(perms[0].value).isEqualTo("*")
        assertThat(perms[1].name).isEqualTo("reports.review")
        assertThat(perms[1].value).isEqualTo("report:r1")
    }

    @Test
    fun `getGlobalReportReaderGroups orders user group view models alphabetically`()
    {
        val repo = mock<RoleRepository> {
            on { getGlobalReportReaderRoles() } doReturn listOf(
                    Role("Science", listOf(), listOf()),
                    Role("Funders", listOf(), listOf()),
                    Role("Tech", listOf(), listOf())
            )
        }

        val sut = RoleController(mock(), repo)
        val result = sut.getGlobalReportReaders()
        assertThat(result[0].name).isEqualTo("Funders")
        assertThat(result[1].name).isEqualTo("Science")
        assertThat(result[2].name).isEqualTo("Tech")
    }

    @Test
    fun `getGlobalReportReaderGroups orders user group view model members alphabetically`()
    {
        val repo = mock<RoleRepository> {
            on { getGlobalReportReaderRoles() } doReturn listOf(
                    Role("Science", listOf(
                            User("c.user", "C User", "testc@example.com"),
                            User("a.user", "A User", "test@example.com"),
                            User("b.user", "B User", "testb@example.com")
                        ),listOf()
                    )
            )
        }

        val sut = RoleController(mock(), repo)
        val members = sut.getGlobalReportReaders()[0].members
        assertThat(members.map { it.username }).containsExactly("a.user", "b.user", "c.user")
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
                    ), listOf()))
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
            on { getScopedReportReaderRoles("r1") } doReturn listOf(
                    Role("Science", listOf(), listOf()),
                    Role("Funders", listOf(), listOf()),
                    Role("Tech", listOf(), listOf())
            )
        }

        val sut = RoleController(actionContextWithReport, repo)
        val result = sut.getScopedReportReaders()
        assertThat(result[0].name).isEqualTo("Funders")
        assertThat(result[1].name).isEqualTo("Science")
        assertThat(result[2].name).isEqualTo("Tech")
    }

    @Test
    fun `getScopedReportReaders orders user group view model members alphabetically`()
    {
        val repo = mock<RoleRepository> {
            on { getScopedReportReaderRoles("r1") } doReturn listOf(
                    Role("Science", listOf(
                            User("c.user", "C User", "testc@example.com"),
                            User("a.user", "A User", "test@example.com"),
                            User("b.user", "B User", "testb@example.com")
                        ), listOf()
                    ))
        }

        val sut = RoleController(actionContextWithReport,repo)
        val members = sut.getScopedReportReaders()[0].members
        assertThat(members.map { it.username }).containsExactly("a.user", "b.user", "c.user")
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