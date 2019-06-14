package org.vaccineimpact.orderlyweb.tests.unit_tests.db

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.jooq.JoinType
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.*
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.vaccineimpact.orderlyweb.db.Tables.*
import java.lang.IllegalArgumentException

class JoinPathTests: TeamcityTests()
{
    @Test
    fun `JoinPathStep throws MissingRelationsBetweenTables when no immediate relationship`()
    {
        assertThatThrownBy{ JoinPathStep(ORDERLYWEB_USER, REPORT) }.isInstanceOf(MissingRelationBetweenTables::class.java)
    }

    @Test
    fun `JoinPathStep throws AmbiguousRelationsBetweenTables when multiple immediate relationships`()
    {
        assertThatThrownBy{ JoinPathStep(CHANGELOG, REPORT_VERSION) }.isInstanceOf(AmbiguousRelationBetweenTables::class.java)
    }

    @Test
    fun `joinPath method does join`()
    {
        JooqContext().use {
            val select = it.dsl.select(REPORT_VERSION.ID).from(REPORT_VERSION)

            val selectJoinStep = select.joinPath(REPORT, joinType = JoinType.LEFT_OUTER_JOIN)

            assertThat(selectJoinStep.isExecutable).isTrue()
        }

    }

    @Test
    fun `getOther returns expected result`()
    {
        val a = REPORT
        val b = REPORT_VERSION

        assertThat(a.getOther(a, b)).isSameAs(b)
        assertThat(a.getOther(b, a)).isSameAs(b)
    }

    @Test
    fun `getOther throws IllegalArgumentException if called on neither argument`()
    {
        assertThatThrownBy { REPORT.getOther(REPORT_VERSION, CHANGELOG) }.isInstanceOf(IllegalArgumentException::class.java)
    }
}