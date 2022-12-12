package org.vaccineimpact.orderlyweb.tests.unit_tests.models

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.PermissionSet
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission

class PermissionSetTests
{

    val perms = setOf(
            ReifiedPermission("testName", Scope.Global()),
            ReifiedPermission("anotherTestName", Scope.Specific("testPrefix", "testId"))
    )
    @Test
    fun `can create from set of permissions`()
    {
        val sut = PermissionSet(perms)

        assertThat(sut.permissions).isSameAs(perms)
    }

    @Test
    fun `can create from raw permissions`()
    {
        val sut = PermissionSet(listOf("*/testName", "testPrefix:testId/anotherTestName"))

        assertThat(sut.permissions).isEqualTo(perms)
    }

    @Test
    fun `plus adds other PermissionSet`()
    {
        val other = PermissionSet(
                setOf(ReifiedPermission("plusPerm", Scope.Global()))
        )
        val sut = PermissionSet(perms).plus(other)

        assertThat(sut.count()).isEqualTo(3)
        assertThat(sut.contains(ReifiedPermission("testName", Scope.Global())))
        assertThat(sut.contains(ReifiedPermission("anotherTestName", Scope.Specific("testPrefix", "testId"))))
        assertThat(sut.contains(ReifiedPermission("plusPerm", Scope.Global())))
    }

    @Test
    fun `plus adds ReifiedPermission`()
    {
        val sut = PermissionSet(perms).plus(ReifiedPermission("plusPerm", Scope.Global()))
        assertThat(sut.count()).isEqualTo(3)
        assertThat(sut.contains(ReifiedPermission("testName", Scope.Global())))
        assertThat(sut.contains(ReifiedPermission("anotherTestName", Scope.Specific("testPrefix", "testId"))))
        assertThat(sut.contains(ReifiedPermission("plusPerm", Scope.Global())))
    }

    @Test
    fun `plus adds permission from string`()
    {
        val sut = PermissionSet(perms).plus("*/plusPerm")
        assertThat(sut.count()).isEqualTo(3)
        assertThat(sut.contains(ReifiedPermission("testName", Scope.Global())))
        assertThat(sut.contains(ReifiedPermission("anotherTestName", Scope.Specific("testPrefix", "testId"))))
        assertThat(sut.contains(ReifiedPermission("plusPerm", Scope.Global())))
    }

    @Test
    fun `toString returns expected result`()
    {
        val sut = PermissionSet(perms)
        assertThat(sut.toString()).isEqualTo("[*/testName, testPrefix:testId/anotherTestName]")
    }

}