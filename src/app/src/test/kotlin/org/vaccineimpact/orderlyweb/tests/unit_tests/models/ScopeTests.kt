package org.vaccineimpact.orderlyweb.tests.unit_tests.models

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.vaccineimpact.orderlyweb.viewmodels.PermissionViewModel

class ScopeTests: TeamcityTests()
{
    @Test
    fun `GlobalScope encompasses is true`()
    {
        val sut = Scope.Global()
        assertThat(sut.encompasses(Scope.Global())).isTrue()
        assertThat(sut.encompasses(Scope.Specific("testPrefix", "testId")))
    }

    @Test
    fun `SpecificScope encompasses is true if identical`()
    {
        val sut = Scope.Specific("testPrefix", "testId")
        assertThat(sut.encompasses(Scope.Specific("testPrefix", "testId"))).isTrue()
    }

    @Test
    fun `SpecificScope encompasses is false if not identical`()
    {
        val sut = Scope.Specific("testPrefix", "testId")
        assertThat(sut.encompasses(Scope.Specific("anotherTestPrefix", "testId"))).isFalse()
        assertThat(sut.encompasses(Scope.Specific("testPrefix", "anotherTestId"))).isFalse()
        assertThat(sut.encompasses(Scope.Global())).isFalse()
    }

    @Test
    fun `SpecificScope equals is true if identical`()
    {
        val sut = Scope.Specific("testPrefix", "testId")
        assertThat(sut.equals(Scope.Specific("testPrefix", "testId"))).isTrue()
    }

    @Test
    fun `SpecificScope equals is false if not identical`()
    {
        val sut = Scope.Specific("testPrefix", "testId")
        assertThat(sut.equals(Scope.Specific("anotherTestPrefix", "testId"))).isFalse()
        assertThat(sut.equals(Scope.Specific("testPrefix", "anotherTestId"))).isFalse()
        assertThat(sut.equals(Scope.Global())).isFalse()
    }

    @Test
    fun `can parse from String`()
    {
        val global = Scope.parse("*")
        assertThat(global).isInstanceOf(Scope.Global::class.java)

        val specific = Scope.parse("testPrefix:testId")
        assertThat(specific).isInstanceOf(Scope.Specific::class.java)
        assertThat((specific as Scope.Specific).databaseScopePrefix).isEqualTo("testPrefix")
        assertThat(specific.databaseScopeId).isEqualTo("testId")
    }

    @Test
    fun `can parse from AssociatePermission`()
    {
        val globalPerm = PermissionViewModel("perm", null, null, "")
        assertThat(Scope.parse(globalPerm)).isInstanceOf(Scope.Global::class.java)

        val specificPerm = PermissionViewModel("perm", "testPrefix", "testId", "")
        val specificScope = Scope.parse(specificPerm)
        assertThat(specificScope).isInstanceOf(Scope.Specific::class.java)
        assertThat((specificScope as Scope.Specific).databaseScopePrefix).isEqualTo("testPrefix")
        assertThat(specificScope.databaseScopeId).isEqualTo("testId")
    }
}