package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web

import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions.assertThat
// import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
// import org.mockito.ArgumentCaptor
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.LogsController
// import org.vaccineimpact.orderlyweb.db.repositories.AuthorizationRepository
// import org.vaccineimpact.orderlyweb.db.repositories.RoleRepository
import org.vaccineimpact.orderlyweb.db.repositories.ReportRunRepository
// import org.vaccineimpact.orderlyweb.errors.BadRequest
// import org.vaccineimpact.orderlyweb.models.Scope
// import org.vaccineimpact.orderlyweb.models.User
// import org.vaccineimpact.orderlyweb.models.permissions.PermissionSet
// import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
// import org.vaccineimpact.orderlyweb.models.permissions.Role
import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.models.Running
import java.time.Instant


class LogsControllerTests
{
    @Test
    fun `gets all running reports`()
    {
        val profile = mock<CommonProfile> {
            on { this.id } doReturn "test@test.com"
        } 
        val context = mock<ActionContext>{
            on { this.userProfile } doReturn profile
        }
        val runningObject = Running(Instant.now(), "name", "key")
        
        val repo = mock<ReportRunRepository> {
            on { this.getAllRunningReports("test@test.com") } doReturn (listOf(runningObject))
        }

        val sut = LogsController(context, repo)

        assertThat(sut.running()).containsExactlyElementsOf(listOf(runningObject))
    }

}