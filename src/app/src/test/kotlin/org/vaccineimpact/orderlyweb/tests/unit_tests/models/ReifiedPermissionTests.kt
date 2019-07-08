package org.vaccineimpact.orderlyweb.tests.unit_tests.models

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.errors.ReifiedPermissionParseException
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests

class ReifiedPermissionTests: TeamcityTests()
{
    @Test
    fun `can parse`()
    {
        val global = ReifiedPermission.parse("*/testPerm")
        assertThat(global.name).isEqualTo("testPerm")
        assertThat(global.scope).isInstanceOf(Scope.Global::class.java)

        val specific = ReifiedPermission.parse("testPrefix:testId/testPerm")
        assertThat(specific.name).isEqualTo("testPerm")
        assertThat(specific.scope).isInstanceOf(Scope.Specific::class.java)
        assertThat((specific.scope as Scope.Specific).databaseScopePrefix).isEqualTo("testPrefix")
        assertThat((specific.scope as Scope.Specific).databaseScopeId).isEqualTo("testId")
    }

    @Test
    fun `throws ReifiedPermissionParseException when cannot parse`()
    {
        assertThatThrownBy { ReifiedPermission.parse("invalid perm") }
                .isInstanceOf(ReifiedPermissionParseException::class.java)
    }

    @Test
    fun `equals is true if identical`()
    {
        val global = ReifiedPermission.parse("*/testPerm")
        assertThat(global.equals(ReifiedPermission.parse("*/testPerm"))).isTrue()

        val specific = ReifiedPermission.parse("testPrefix:testId/testPerm")
        assertThat(specific.equals(ReifiedPermission.parse("testPrefix:testId/testPerm"))).isTrue()
    }

    @Test
    fun `equals is false if not identical`()
    {
        val sut = ReifiedPermission.parse("testPrefix:testId/testPerm")
        assertThat(sut.equals(ReifiedPermission.parse("anotherTestPrefix:testId/testPerm"))).isFalse()
        assertThat(sut.equals(ReifiedPermission.parse("testPrefix:anotherTestId/testPerm"))).isFalse()
        assertThat(sut.equals(ReifiedPermission.parse("*/testPerm"))).isFalse()
        assertThat(sut.equals(ReifiedPermission.parse("testPrefix:testId/wrongPerm"))).isFalse()
    }


}