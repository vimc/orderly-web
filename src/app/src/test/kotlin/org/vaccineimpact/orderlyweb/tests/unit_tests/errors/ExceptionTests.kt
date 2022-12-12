package org.vaccineimpact.orderlyweb.tests.unit_tests.errors

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.vaccineimpact.orderlyweb.errors.*

class ExceptionTests
{
    @Test
    fun `can create UnsupportedValueException`()
    {
        val sut = UnsupportedValueException("test")
        Assertions.assertThat(sut.message).isEqualTo(
                "Unsupported value 'test' of type 'String'")

    }

    @Test
    fun `can create PermisionRequirementParseException`()
    {
        val sut = PermissionRequirementParseException("test bad permission")
        Assertions.assertThat(sut.message).isEqualTo(
                "Unable to parse 'test bad permission' as a PermissionRequirement. " +
                        "It should have the form 'scope/name' where scope is either the global scope '*' " +
                        "or a specific scope identifier in the form 'prefix:<urlKey>'")
    }

    @Test
    fun `can create ReifiedPermissionParseException`()
    {
        val sut = ReifiedPermissionParseException("invalid")
        Assertions.assertThat(sut.message).isEqualTo("Unable to parse 'invalid' as a ReifiedPermission. " +
                "It should have the form 'scope/name' where scope is either the global scope '*' " +
                "or a specific scope identifier in the form 'prefix:id'")
    }

    @Test
    fun `can create BadRequest with list of errors`()
    {
        val sut = BadRequest(listOf("error1", "error2"))
        Assertions.assertThat(sut.httpStatus).isEqualTo(400)
        Assertions.assertThat(sut.message).isEqualTo("the following problems occurred:\nerror1\nerror2")
    }

    @Test
    fun `can create BadRequest with single error`()
    {
        val sut = BadRequest("only error")
        Assertions.assertThat(sut.httpStatus).isEqualTo(400)
        Assertions.assertThat(sut.message).isEqualTo("the following problems occurred:\nonly error")
    }
}
