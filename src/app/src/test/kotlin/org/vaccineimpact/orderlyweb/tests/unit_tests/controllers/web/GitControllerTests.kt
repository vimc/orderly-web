package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.controllers.web.GitController
import org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.api.ControllerTest

class GitControllerTests: ControllerTest()
{
    @Test
    fun `gets commits for branch`()
    {
        val sut = GitController(mock())
        val result = sut.getCommits()
        assertThat(result.count()).isEqualTo(2)
    }
}