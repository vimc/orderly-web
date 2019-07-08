package org.vaccineimpact.orderlyweb.tests.unit_tests.models

import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.models.permissions.UserGroupPermission
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests

class UserGroupPermissionsTests : TeamcityTests()
{
    @Test
    fun `equals is true if identical`()
    {
        val sut = UserGroupPermission("groupId", ReifiedPermission.parse("*/testPerm"))

        Assertions.assertThat(sut.equals(UserGroupPermission("groupId", ReifiedPermission.parse("*/testPerm")))).isTrue()
    }

    @Test
    fun `equals is false if not identical`()
    {
        val sut = UserGroupPermission("groupId", ReifiedPermission.parse("testPrefix:testId/testPerm"))

        Assertions.assertThat(sut.equals(
                UserGroupPermission("groupId",
                        ReifiedPermission.parse("*/testPerm")))).isFalse()
        Assertions.assertThat(sut.equals(
                UserGroupPermission("anotherId",
                        ReifiedPermission.parse("testPrefix:testId/testPerm")))).isFalse()
    }
}