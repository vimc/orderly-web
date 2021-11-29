package org.vaccineimpact.orderlyweb.tests.unit_tests.logic

import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.vaccineimpact.orderlyweb.OrderlyServerAPI
import org.vaccineimpact.orderlyweb.errors.BadRequest
import org.vaccineimpact.orderlyweb.logic.OrderlyWebWorkflowLogic
import org.vaccineimpact.orderlyweb.models.Parameter

class WorkflowLogicTests
{
    private fun sut(orderly: OrderlyServerAPI) = OrderlyWebWorkflowLogic(orderly)

    private val testBranch = "testBranch"
    private val testCommit = "testCommit"
    private val mockParams = listOf(Parameter("disease", "defaultDisuses"), Parameter("year", "2020"))
    private val commitOnlyQs = mapOf("commit" to testCommit)
    private val mockTestOrderlyAPI = mock<OrderlyServerAPI> {
        on { getRunnableReportNames(eq(mapOf("branch" to testBranch, "commit" to testCommit))) } doReturn listOf(
                "test1", "test2", "test3")
        on { getReportParameters(eq("test1"), eq(commitOnlyQs)) } doReturn mockParams
        on { getReportParameters(eq("test2"), eq(commitOnlyQs)) } doReturn mockParams
        on { getReportParameters(eq("test3"), eq(commitOnlyQs)) } doReturn mockParams
    }

    @Test
    fun `can parse valid workflow CSV`()
    {
        val csvReader = """
            report,disease,year
            test1,HepB,2020
            test2,,2021
            test3,Rubella,
        """.trimIndent().reader()

        val result = sut(mockTestOrderlyAPI).parseAndValidateWorkflowCSV(csvReader, testBranch, testCommit)
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
        val mockOrderlyAPI = mock<OrderlyServerAPI> {
            on { getRunnableReportNames(eq(mapOf("branch" to testBranch, "commit" to testCommit))) } doReturn listOf(
                    "test1", "test2", "test3")
            on { getReportParameters(any(), eq(mapOf("commit" to testCommit))) } doReturn listOf<Parameter>()
        }
        val result = sut(mockOrderlyAPI).parseAndValidateWorkflowCSV(csvReader, testBranch, testCommit)
        assertThat(result.count()).isEqualTo(2)
        assertThat(result[0].name).isEqualTo("test1")
        assertThat(result[0].params).isEqualTo(mapOf<String, String>())
        assertThat(result[1].name).isEqualTo("test2")
        assertThat(result[1].params).isEqualTo(mapOf<String, String>())
    }

    @Test
    fun `validate method omits commit and branch parameters when null`()
    {
        val csvReader = """
            report
            test1
        """.trimIndent().reader()
        val mockOrderlyAPI = mock<OrderlyServerAPI> {
            on { getRunnableReportNames(eq(mapOf())) } doReturn listOf("test1")
            on { getReportParameters(eq("test1"), eq(mapOf())) } doReturn listOf<Parameter>()
        }
        val result = sut(mockOrderlyAPI).parseAndValidateWorkflowCSV(csvReader, null, null)
        assertThat(result.count()).isEqualTo(1)
        assertThat(result[0].name).isEqualTo("test1")
        assertThat(result[0].params).isEqualTo(mapOf<String, String>())

        verify(mockOrderlyAPI).getRunnableReportNames(eq(mapOf()))
        verify(mockOrderlyAPI).getReportParameters(eq("test1"), eq(mapOf()))
    }

    @Test
    fun `throws expected error when parse CSV with no rows`()
    {
        val csvReader = "".reader()
        assertThatThrownBy{ sut(mock()).parseAndValidateWorkflowCSV(csvReader, "","") }
                .isInstanceOf(BadRequest::class.java).hasMessageContaining("File contains no rows")
    }

    @Test
    fun `throws expected error when parse CSV with no reports`()
    {
        val csvReader = "report,param1".reader()
        assertThatThrownBy{ sut(mock()).parseAndValidateWorkflowCSV(csvReader, "", "") }
                .isInstanceOf(BadRequest::class.java).hasMessageContaining("File contains no reports")
    }

    @Test
    fun `throws expected error when parse CSV where incorrect first header`()
    {
        val csvReader = "report1,param1".reader()
        assertThatThrownBy{ sut(mock()).parseAndValidateWorkflowCSV(csvReader, "", "") }
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
        assertThatThrownBy{ sut(mockTestOrderlyAPI).parseAndValidateWorkflowCSV(csvReader, testBranch, testCommit) }
                .isInstanceOf(BadRequest::class.java).hasMessageContaining(
                        "Row 3: row should contain 3 values, 4 values found")
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
        assertThatThrownBy{ sut(mockTestOrderlyAPI).parseAndValidateWorkflowCSV(csvReader, testBranch, testCommit) }
                .isInstanceOf(BadRequest::class.java).hasMessageContaining(
                        "Row 4: row should contain 3 values, 2 values found")
    }

    @Test
    fun `throws expected errors whe validate CSV which has report which do not exist in Orderly`()
    {
        val csvReader = """
            report,disease,year
            nonexistent1,,
            test1,HepB,2020
            nonexistent2,HepB,2020
        """.trimIndent().reader()
        assertThatThrownBy{ sut(mockTestOrderlyAPI).parseAndValidateWorkflowCSV(csvReader, testBranch, testCommit) }
                .isInstanceOf(BadRequest::class.java).hasMessageContaining("""
                    Row 2, column 1: report 'nonexistent1' not found in Orderly
                    Row 4, column 1: report 'nonexistent2' not found in Orderly""".trimIndent())
    }

    @Test
    fun `throws expected errors when validate CSV with parameters which do not match Orderly reports`()
    {
        val csvReader = """
            report,disease,year,age
            SingleDefaultParam,HepB,2020,5 
            SingleDefaultParam,,,
            SingleNoDefaultParam,,,
            TwoParamsOneDefault,,2021,
            TwoParamsOneDefault,Cholera,,5
            TwoParamsNoDefault,,,
        """.trimIndent().reader()

        val mockOrderlyAPI = mock<OrderlyServerAPI> {
            on { getRunnableReportNames(eq(mapOf("branch" to testBranch, "commit" to testCommit))) } doReturn listOf(
                    "SingleDefaultParam", "SingleNoDefaultParam", "TwoParamsOneDefault", "TwoParamsNoDefault")
            on { getReportParameters(eq("SingleDefaultParam"), eq(commitOnlyQs)) } doReturn listOf(
                    Parameter("disease", "default"))
            on { getReportParameters(eq("SingleNoDefaultParam"), eq(commitOnlyQs)) } doReturn listOf(
                    Parameter("disease", null))
            on { getReportParameters(eq("TwoParamsOneDefault"), eq(commitOnlyQs)) } doReturn listOf(
                    Parameter("year", null), Parameter("age", "1")
            )
            on { getReportParameters(eq("TwoParamsNoDefault"), eq(commitOnlyQs)) } doReturn listOf(
                    Parameter("year", null), Parameter("age", null)
            )
        }
        assertThatThrownBy{ sut(mockOrderlyAPI).parseAndValidateWorkflowCSV(csvReader, testBranch, testCommit) }
                .isInstanceOf(BadRequest::class.java).hasMessageContaining("""
                    Row 2, column 3: unexpected parameter 'year' provided for report 'SingleDefaultParam'
                    Row 2, column 4: unexpected parameter 'age' provided for report 'SingleDefaultParam'
                    Row 4, column 2: required parameter 'disease' was not provided for report 'SingleNoDefaultParam'
                    Row 6, column 3: required parameter 'year' was not provided for report 'TwoParamsOneDefault'
                    Row 6, column 2: unexpected parameter 'disease' provided for report 'TwoParamsOneDefault'
                    Row 7, column 3: required parameter 'year' was not provided for report 'TwoParamsNoDefault'
                    Row 7, column 4: required parameter 'age' was not provided for report 'TwoParamsNoDefault'""".trimIndent())
    }
}
