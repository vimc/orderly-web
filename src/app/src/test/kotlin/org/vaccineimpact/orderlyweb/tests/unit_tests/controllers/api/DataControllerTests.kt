package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.api

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.FileSystem
import org.vaccineimpact.orderlyweb.controllers.api.DataController
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.errors.OrderlyFileNotFoundError

class DataControllerTests : ControllerTest()
{
    private val mockConfig = mock<Config> {
        on { this.get("orderly.root") } doReturn "root/"
    }

    @Test
    fun `gets data for report`()
    {
        val name = "testname"
        val version = "testversion"

        val data = mapOf("test.csv" to "hjkdasjkldas6762i1j")

        val orderly = mock<OrderlyClient> {
            on { this.getData(name, version) } doReturn data
        }

        val actionContext = mock<ActionContext> {
            on { this.params(":name") } doReturn name
            on { this.params(":version") } doReturn version
        }

        val sut = DataController(actionContext, orderly, mock<FileSystem>(), mockConfig)

        assertThat(sut.get()).isEqualTo(data)
    }

    @Test
    fun `gets csv file for report by name if type not specified`()
    {

        val name = "testname"
        val version = "testversion"
        val datumname = "testname"
        val hash = "hjkdasjkldas6762i1j"

        val orderly = mock<OrderlyClient> {
            on { this.getDatum(name, version, datumname) } doReturn hash
        }

        val fileSystem = mock<FileSystem>() {
            on { this.fileExists("root/data/csv/$hash.csv") } doReturn true
        }

        val actionContext = mock<ActionContext> {
            on { this.params(":name") } doReturn name
            on { this.params(":version") } doReturn version
            on { this.params(":data") } doReturn datumname
            on { this.getSparkResponse() } doReturn mockSparkResponse
        }

        val sut = DataController(actionContext, orderly, fileSystem, mockConfig)
        sut.downloadData()

        verify(fileSystem, times(1)).writeFileToOutputStream("root/data/csv/$hash.csv", mockOutputStream)
    }

    @Test
    fun `gets rds data file for report by name`()
    {

        val name = "testname"
        val version = "testversion"
        val datumname = "testname"
        val hash = "hjkdasjkldas6762i1j"

        val orderly = mock<OrderlyClient> {
            on { this.getDatum(name, version, datumname) } doReturn hash
        }

        val fileSystem = mock<FileSystem>() {
            on { this.fileExists("root/data/rds/$hash.rds") } doReturn true
        }

        val actionContext = mock<ActionContext> {
            on { this.params(":name") } doReturn name
            on { this.params(":version") } doReturn version
            on { this.params(":data") } doReturn datumname
            on { this.getSparkResponse() } doReturn mockSparkResponse
            on { this.queryParams("type") } doReturn "rds"
        }

        val sut = DataController(actionContext, orderly, fileSystem, mockConfig)
        sut.downloadData()

        verify(fileSystem, times(1)).writeFileToOutputStream("root/data/rds/$hash.rds", mockOutputStream)
    }

    @Test
    fun `gets csv file by id`()
    {

        val hash = "hjkdasjkldas6762i1j"

        val fileSystem = mock<FileSystem>() {
            on { this.fileExists("root/data/csv/$hash.csv") } doReturn true
        }

        val actionContext = mock<ActionContext> {
            on { this.getSparkResponse() } doReturn mockSparkResponse
            on { this.params(":id") } doReturn hash
        }

        val sut = DataController(actionContext, mock<OrderlyClient>(), fileSystem, mockConfig)
        sut.downloadCSV()

        verify(fileSystem, times(1)).writeFileToOutputStream("root/data/csv/$hash.csv", mockOutputStream)
    }

    @Test
    fun `gets rds file by id`()
    {

        val hash = "hjkdasjkldas6762i1j"

        val fileSystem = mock<FileSystem>() {
            on { this.fileExists("root/data/rds/$hash.rds") } doReturn true
        }

        val actionContext = mock<ActionContext> {
            on { this.getSparkResponse() } doReturn mockSparkResponse
            on { this.params(":id") } doReturn hash
        }

        val sut = DataController(actionContext, mock<OrderlyClient>(), fileSystem, mockConfig)
        sut.downloadRDS()

        verify(fileSystem, times(1)).writeFileToOutputStream("root/data/rds/$hash.rds", mockOutputStream)
    }

    @Test
    fun `throws unknown object error if data does not exist for report`()
    {
        val name = "testname"
        val version = "testversion"

        val data = mapOf("test.csv" to "hjkdasjkldas6762i1j")

        val orderly = mock<OrderlyClient> {
            on { this.getData(name, version) } doReturn data
        }

        val actionContext = mock<ActionContext> {
            on { this.params(":name") } doReturn name
            on { this.params(":version") } doReturn version
        }

        val sut = DataController(actionContext, orderly, mock<FileSystem>(), mockConfig)

        assertThat(sut.get()).isEqualTo(data)
    }

    @Test
    fun `throws file not found error if file does not exist for report`()
    {
        val name = "testname"
        val version = "testversion"
        val data = "testdata"

        val orderly = mock<OrderlyClient> {
            on { this.getDatum(name, version, data) } doReturn "64387yhfdjsbc"
        }

        val actionContext = mock<ActionContext> {
            on { this.params(":name") } doReturn name
            on { this.params(":version") } doReturn version
            on { this.params(":data") } doReturn data
            on { this.queryParams("type") } doReturn "csv"
            on { this.getSparkResponse() } doReturn mockSparkResponse
        }

        val sut = DataController(actionContext, orderly, mock<FileSystem>(), mockConfig)

        assertThatThrownBy { sut.downloadData() }
                .isInstanceOf(OrderlyFileNotFoundError::class.java)
    }
}
