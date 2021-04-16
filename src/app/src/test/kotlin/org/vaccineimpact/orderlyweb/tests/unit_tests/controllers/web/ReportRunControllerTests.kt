package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web

import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.ReportRunController
import org.vaccineimpact.orderlyweb.db.repositories.ReportRunRepository
import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.models.ReportRunWithDate
import java.time.Instant


class ReportRunControllerTests
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
        val runningObject = ReportRunWithDate("name", "key", Instant.now())
        
        val repo = mock<ReportRunRepository> {
            on { this.getAllReportRunsForUser("test@test.com") } doReturn (listOf(runningObject))
        }

        val sut = ReportRunController(context, repo)

        assertThat(sut.runningReports()).containsExactlyElementsOf(listOf(runningObject))
    }

}