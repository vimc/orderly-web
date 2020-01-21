package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.api

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.api.UserController
import org.vaccineimpact.orderlyweb.db.UserRepository
import org.vaccineimpact.orderlyweb.errors.MissingParameterError
import org.vaccineimpact.orderlyweb.models.UserSource
import org.vaccineimpact.orderlyweb.security.WebTokenHelper

class UserControllerTests
{
    @Test
    fun `adds user`()
    {
        val actionContext = mock<ActionContext> {
            on { this.postData("email") } doReturn "test@test.com"
            on { this.postData("username") } doReturn "firstname.lastname"
            on { this.postData("displayName") } doReturn "Firstname Lastname"
            on { this.postData("source") } doReturn "Montagu"
        }
        val mockRepo = mock<UserRepository>()

        val sut = UserController(actionContext, WebTokenHelper.instance, mockRepo)
        sut.addUser()
        verify(mockRepo).addUser("test@test.com", "firstname.lastname", "Firstname Lastname",
                                    UserSource.Montagu)
    }
}