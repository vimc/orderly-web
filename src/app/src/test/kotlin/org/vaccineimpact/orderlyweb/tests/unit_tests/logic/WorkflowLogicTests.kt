package org.vaccineimpact.orderlyweb.tests.unit_tests.logic

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.vaccineimpact.orderlyweb.errors.BadRequest
import org.vaccineimpact.orderlyweb.logic.OrderlyWebWorkflowLogic

class WorkflowLogicTests
{
    val sut = OrderlyWebWorkflowLogic()

    @Test
    fun `can parse valid workflow CSV`()
    {
        val csvReader = """
            report,disease,year
            test1,HepB,2020
            test2,,2021
            test3,Rubella,
        """.trimIndent().reader()
        val result = sut.parseAndValidateWorkflowCSV(csvReader, "", "", mock())
        assertThat(result.count()).isEqualTo(3)
        assertThat(result[0].name).isEqualTo("test1")
        assertThat(result[0].params).isEqualTo(mapOf("disease" to "HepB", "year" to "2020"))
        assertThat(result[1].name).isEqualTo("test2")
        assertThat(result[1].params).isEqualTo(mapOf("year" to "2021"))
        assertThat(result[2].name).isEqualTo("test3")
        assertThat(result[2].params).isEqualTo(mapOf("disease" to "Rubella"))
    }

    @Test
    fun `can parse valid workflow CSV with no params`()
    {
        val csvReader = """
            report
            test1
            test2
        """.trimIndent().reader()
        val result = sut.parseAndValidateWorkflowCSV(csvReader, "", "", mock())
        assertThat(result.count()).isEqualTo(2)
        assertThat(result[0].name).isEqualTo("test1")
        assertThat(result[0].params).isEqualTo(mapOf<String, String>())
        assertThat(result[1].name).isEqualTo("test2")
        assertThat(result[1].params).isEqualTo(mapOf<String, String>())
    }

    @Test
    fun `throws expected error when parse CSV with no rows`()
    {
        val csvReader = "".reader()
        assertThatThrownBy{ sut.parseAndValidateWorkflowCSV(csvReader, "","", mock()) }
                .isInstanceOf(BadRequest::class.java).hasMessageContaining("File contains no rows")
    }

    @Test
    fun `throws expected error when parse CSV with no reports`()
    {
        val csvReader = "report,param1".reader()
        assertThatThrownBy{ sut.parseAndValidateWorkflowCSV(csvReader, "", "", mock()) }
                .isInstanceOf(BadRequest::class.java).hasMessageContaining("File contains no reports")
    }

    @Test
    fun `throws expected error when parse CSV where incorrect first header`()
    {
        val csvReader = "report1,param1".reader()
        assertThatThrownBy{ sut.parseAndValidateWorkflowCSV(csvReader, "", "", mock()) }
                .isInstanceOf(BadRequest::class.java).hasMessageContaining("First header must be 'report'")
    }

    @Test
    fun `throws expected error when parse CSV with row with too many values()`()
    {
        val csvReader = """
            report,disease,year
            test1,HepB,2020
            test2,,2021,TOO_MANY
            test3,Rubella,
        """.trimIndent().reader()
        assertThatThrownBy{ sut.parseAndValidateWorkflowCSV(csvReader, "", "", mock()) }
                .isInstanceOf(BadRequest::class.java).hasMessageContaining("Report row 2 should contain 3 values, 4 values found")
    }

    @Test
    fun `throws expected error when parse CSV with row with too few values`()
    {
        val csvReader = """
            report,disease,year
            test1,HepB,2020
            test2,,2021
            test3,Rubella
        """.trimIndent().reader()
        assertThatThrownBy{ sut.parseAndValidateWorkflowCSV(csvReader, "", "", mock()) }
                .isInstanceOf(BadRequest::class.java).hasMessageContaining("Report row 3 should contain 3 values, 2 values found")
    }
}
