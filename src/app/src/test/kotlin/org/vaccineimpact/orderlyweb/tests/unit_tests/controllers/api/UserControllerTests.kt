package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.api

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.api.UserController
import org.vaccineimpact.orderlyweb.db.repositories.UserRepository
import org.vaccineimpact.orderlyweb.models.UserSource
import org.vaccineimpact.orderlyweb.security.WebTokenHelper

class UserControllerTests
{
    @Test
    fun `adds user`()
    {
        val actionContext = mock<ActionContext> {
            on { this.postData<String>("email") } doReturn "test@test.com"
            on { this.postData<String>("username") } doReturn "firstname.lastname"
            on { this.postData<String>("displayName") } doReturn "Firstname Lastname"
            on { this.postData<String>("source") } doReturn "Montagu"
        }
        val mockRepo = mock<UserRepository>()

        val sut = UserController(actionContext, WebTokenHelper.instance, mockRepo)
        val result = sut.addUser()
        verify(mockRepo).addUser("test@test.com", "firstname.lastname", "Firstname Lastname",
                UserSource.Montagu)
        assertThat(result).isEqualToIgnoringCase("ok")
    }
}
