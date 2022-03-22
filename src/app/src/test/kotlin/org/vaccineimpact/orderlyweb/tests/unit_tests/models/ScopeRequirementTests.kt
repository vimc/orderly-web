package org.vaccineimpact.orderlyweb.tests.unit_tests.models

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.vaccineimpact.orderlyweb.models.ScopeRequirement

class ScopeRequirementTests
{
    @Test
    fun `can parse specific`()
    {
        val sut = ScopeRequirement.parse("testPrefix:<testIdKey>")
        assertThat(sut).isInstanceOf(ScopeRequirement.Specific::class.java)
        assertThat((sut as ScopeRequirement.Specific).prefix).isEqualTo("testPrefix")
        assertThat(sut.scopeIdUrlKey).isEqualTo("testIdKey")
    }

    @Test
    fun `can parse global`()
    {
        val sut = ScopeRequirement.parse("*")
        assertThat(sut).isInstanceOf(ScopeRequirement.Global::class.java)
    }

    @Test
    fun `throws expected exception when cannot parse`()
    {
        assertThatThrownBy { ScopeRequirement.parse("testPrefix:invalidIdKey") }
                .hasMessage("Unable to parse testPrefix:invalidIdKey as a scope requirement - missing angle brackets from scope ID URL key")
    }

    @Test
    fun `equals returns true when ScopeRequirements are identical`()
    {
        var sut = ScopeRequirement.parse("*")
        var other = ScopeRequirement.parse("*")

        assertThat(sut.equals(other)).isTrue()

        sut = ScopeRequirement.parse("testPrefix:<testIdKey>")
        other = ScopeRequirement.parse("testPrefix:<testIdKey>")

        assertThat(sut.equals(other)).isTrue()
    }

    @Test
    fun `equals return false ScopeRequirements are not identical`()
    {
        val sut = ScopeRequirement.parse("testPrefix:<testIdKey>")
        var other = ScopeRequirement.parse("*")

        assertThat(sut.equals(other)).isFalse()

        other = ScopeRequirement.parse("anotherTestPrefix:<testIdKey>")
        assertThat(sut.equals(other)).isFalse()

        other = ScopeRequirement.parse("testPrefix:<anotherTestIdKey>")
        assertThat(sut.equals(other)).isFalse()
    }

    @Test
    fun `can get hashCode`()
    {
        val sut = ScopeRequirement.parse("testPrefix:<testIdKey>")
        assertThat(sut.hashCode()).isEqualTo("testPrefix:<testIdKey>".hashCode())
    }
}
