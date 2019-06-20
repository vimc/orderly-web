package org.vaccineimpact.orderlyweb.tests.unit_tests.errors

import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.orderlyweb.errors.*
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import java.security.Permission

class ExceptionTests: TeamcityTests()
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
}