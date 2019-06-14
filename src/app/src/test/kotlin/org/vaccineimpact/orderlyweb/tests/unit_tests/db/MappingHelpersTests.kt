package org.vaccineimpact.orderlyweb.tests.unit_tests.db

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.parseEnum
import org.vaccineimpact.orderlyweb.errors.UnknownEnumValue
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests

class WebEndpointTests: TeamcityTests()
{
    enum class TestEnum
    {
        TestOne, TestTwo
    }

    @Test
    fun `can parse enum`()
    {
        val enumValue : TestEnum = parseEnum("TestOne")
        assertThat(enumValue).isEqualTo(TestEnum.TestOne)
    }

    @Test
    fun `throws UnknownEnumValue if cannot parse enum`()
    {
        assertThatThrownBy{ parseEnum<TestEnum>("TestThree") }
                .isInstanceOf(UnknownEnumValue::class.java)

    }
}