package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web.ReportControllerTests

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.ReportController
import org.vaccineimpact.orderlyweb.db.repositories.ReportRepository
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests

class PublishTests : TeamcityTests() {

    @Test
    fun `can publish multiple versions`() {
        val mockRepo = mock<ReportRepository>()
        val mockContext = mock<ActionContext> {
            on { postData<List<String>>("ids")} doReturn listOf("v1", "v2")
        }
        val sut = ReportController(mockContext, mock(), mockRepo, mock())
        sut.publishReports()

        verify(mockRepo).publish(listOf("v1", "v2"))
    }
}
