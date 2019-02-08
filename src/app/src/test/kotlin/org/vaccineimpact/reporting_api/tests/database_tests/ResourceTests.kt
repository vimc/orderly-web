package org.vaccineimpact.reporting_api.tests.database_tests

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.vaccineimpact.api.models.FilePurpose
import org.vaccineimpact.reporting_api.db.Orderly
import org.vaccineimpact.reporting_api.db.tables.FileInput
import org.vaccineimpact.reporting_api.errors.UnknownObjectError
import org.vaccineimpact.reporting_api.tests.insertFileInput
import org.vaccineimpact.reporting_api.tests.insertReport

class ResourceTests : CleanDatabaseTests()
{

    private fun createSut(isReviewer: Boolean = false): Orderly
    {
        return Orderly(isReviewer)
    }

    @Test
    fun `returns resourcename if report has resource`()
    {
        val hash = "gfe7064mvdfjieync"
        val name = "resource.csv"
        insertReport("test", "version1", hashResources = "{\"$name\": \"$hash\"}")

        val sut = createSut()

        val result = sut.getResource("test", "version1", name)
        assertThat(result).isEqualTo(hash)
    }

    @Test
    fun `throws unknown object error if report does not have artefact`()
    {
        insertReport("test", "version1", hashResources = "{\"resource.csv\": \"gfe7064mvdfjieync\"}")

        val sut = createSut()

        Assertions.assertThatThrownBy { sut.getResource("test", "version1", "details.csv") }
                .isInstanceOf(UnknownObjectError::class.java)

    }

    @Test
    fun `throws unknown object error if report not published and user not reviewer`()
    {

        val artefactHashString = "{\"summary.csv\":\"07dffb00305279935544238b39d7b14b\"," +
                "\"graph.png\":\"4b89e0b767cee1c30f2e910684189680\"}"

        insertReport("test", "version1", hashArtefacts = artefactHashString, published = false)

        val sut = createSut()

        Assertions.assertThatThrownBy { sut.getResource("test", "version1", "graph.png") }
                .isInstanceOf(UnknownObjectError::class.java)

    }

    @Test
    fun `can get resource filenames`()
    {
        insertReport("test", "version1")
        insertFileInput("version1", "testfile.csv", FilePurpose.RESOURCE)
        val sut = createSut()
        val result = sut.getResourceFileNames("test", "version1")
        assertThat(result[0]).isEqualTo("testfile.csv")
        assertThat(result.count()).isEqualTo(1)
    }

    @Test
    fun `getResourceFileNames does not return resources for other reports`()
    {
        insertReport("test", "version1")
        insertReport("test", "version2")
        insertFileInput("version2", "testfile.csv", FilePurpose.RESOURCE)
        val sut = createSut()
        val result = sut.getResourceFileNames("test", "version1")
        assertThat(result.count()).isEqualTo(0)
    }


    @Test
    fun `getResourceFileNames does not return file inputs that are not resources`()
    {
        insertReport("test", "version1")
        insertFileInput("version1", "testfile.csv", FilePurpose.ORDERLY_YML)
        val sut = createSut()
        val result = sut.getResourceFileNames("test", "version1")
        assertThat(result.count()).isEqualTo(0)
    }

    @Test
    fun `getResourceFileNames throws UnknownObjectError if version does not belong to report`()
    {
        insertReport("test", "version1")
        insertReport("badreport", "badversion")
        insertFileInput("version1", "testfile.csv", FilePurpose.RESOURCE)
        val sut = createSut()

        assertThatThrownBy {
            sut.getResourceFileNames("badreport", "version1")
        }.isInstanceOf(UnknownObjectError::class.java)
    }

    @Test
    fun `getResourceFileNames throws UnknownObjectError if version is not published and user is not a reviewer`()
    {
        insertReport("test", "version1", published = false)
        insertFileInput("version1", "testfile.csv", FilePurpose.RESOURCE)
        val sut = createSut()

        assertThatThrownBy {
            sut.getResourceFileNames("test", "version1")
        }.isInstanceOf(UnknownObjectError::class.java)
    }

    @Test
    fun `report reviewer can get resource file names for unpublished report`()
    {
        insertReport("test", "version1", published = false)
        insertFileInput("version1", "testfile.csv", FilePurpose.RESOURCE)
        val sut = createSut(isReviewer = true)

        sut.getResourceFileNames("test", "version1")
    }
}
