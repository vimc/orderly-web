package org.vaccineimpact.orderlyweb.tests.unit_tests.models

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.vaccineimpact.orderlyweb.errors.PermissionRequirementParseException
import org.vaccineimpact.orderlyweb.models.PermissionRequirement
import org.vaccineimpact.orderlyweb.models.ScopeRequirement

class PermissionRequimentTests
{
    @Test
    fun `can parse`()
    {
        val sut = PermissionRequirement.parse("testPrefix:<testIdKey>/test.req")
        assertThat(sut.name).isEqualTo("test.req")
        assertThat(sut.scopeRequirement.value).isEqualTo("testPrefix:<testIdKey>")
    }

    @Test
    fun `throws PermissionRequirementParseException when cannot parse`()
    {
        assertThatThrownBy { PermissionRequirement.parse("invalid") }
                .isInstanceOf(PermissionRequirementParseException::class.java)
    }

    @Test
    fun `toString returns expected string`()
    {
        val sut = PermissionRequirement("test.req", ScopeRequirement.Global())
        assertThat(sut.toString()).isEqualTo("*/test.req")
    }
}