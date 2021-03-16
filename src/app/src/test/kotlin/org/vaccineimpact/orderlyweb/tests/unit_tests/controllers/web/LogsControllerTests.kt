package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web

import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.LogsController
// import org.vaccineimpact.orderlyweb.db.repositories.AuthorizationRepository
// import org.vaccineimpact.orderlyweb.db.repositories.RoleRepository
import org.vaccineimpact.orderlyweb.db.repositories.ReportRunRepository
import org.vaccineimpact.orderlyweb.errors.BadRequest
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.User
import org.vaccineimpact.orderlyweb.models.permissions.PermissionSet
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.models.permissions.Role

class LogsController
{
    @Test
    fun `gets all running reports`()
    {
        val testUser = User("test.user", "Test user", "test@test.com")
        val repo = mock<ReportRunRepository> {
            on { this.getAllRunningReports(testUser) } doReturn (listOf("one", "two"))
        }

        val sut = LogsController(mock(), repo, mock(), mock())

        assertThat(sut.getAllRunningReports(testUser)).containsExactlyElementsOf(listOf("one", "two"))
    }

}