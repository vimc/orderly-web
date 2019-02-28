package org.vaccineimpact.orderlyweb.tests.database_tests

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.vaccineimpact.orderlyweb.models.FilePurpose
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.tests.insertFileInput
import org.vaccineimpact.orderlyweb.tests.insertReport

class ResourceTests : CleanDatabaseTests()
{

    private fun createSut(isReviewer: Boolean = false): Orderly
    {
        return Orderly(isReviewer)
    }

    @Test
    fun `getResourceHash returns resource hash if report has resource`()
    {
        val name = "resource.csv"
        insertReport("test", "version1")
        insertFileInput("version1", name, FilePurpose.RESOURCE)

        val sut = createSut()

        val result = sut.getResourceHash("test", "version1", name)
        assertThat(result).isNotNull()
    }

    @Test
    fun `getResourceHash throws unknown object error if report does not have given resource`()
    {
        insertReport("test", "version1")
        insertFileInput("version1", "testfile.csv", FilePurpose.RESOURCE)
        val sut = createSut()

        Assertions.assertThatThrownBy { sut.getResourceHash("test", "version1", "details.csv") }
                .isInstanceOf(UnknownObjectError::class.java)

    }

    @Test
    fun `getResourceHashes throws UnknownObjectError if version is not published and user is not a reviewer`()
    {
        insertReport("test", "version1", published = false)
        insertFileInput("version1", "testfile.csv", FilePurpose.RESOURCE)
        val sut = createSut()

        assertThatThrownBy {
            sut.getResourceHashes("test", "version1")
        }.isInstanceOf(UnknownObjectError::class.java)
    }

    @Test
    fun `can get resource hashes`()
    {
        insertReport("test", "version1")
        insertFileInput("version1", "testfile.csv", FilePurpose.RESOURCE)
        val sut = createSut()
        val result = sut.getResourceHashes("test", "version1")
        assertThat(result.keys).containsExactlyElementsOf(listOf("testfile.csv"))
    }

    @Test
    fun `getResourceHashes does not return resources for other reports`()
    {
        insertReport("test", "version1")
        insertReport("test", "version2")
        insertFileInput("version2", "testfile.csv", FilePurpose.RESOURCE)
        val sut = createSut()
        val result = sut.getResourceHashes("test", "version1")
        assertThat(result.count()).isEqualTo(0)
    }


    @Test
    fun `getResourceHashes does not return file inputs that are not resources`()
    {
        insertReport("test", "version1")
        insertFileInput("version1", "testfile.csv", FilePurpose.ORDERLY_YML)
        val sut = createSut()
        val result = sut.getResourceHashes("test", "version1")
        assertThat(result.count()).isEqualTo(0)
    }

    @Test
    fun `getResourceHashess throws UnknownObjectError if version does not belong to report`()
    {
        insertReport("test", "version1")
        insertReport("badreport", "badversion")
        insertFileInput("version1", "testfile.csv", FilePurpose.RESOURCE)
        val sut = createSut()

        assertThatThrownBy {
            sut.getResourceHashes("badreport", "version1")
        }.isInstanceOf(UnknownObjectError::class.java)
    }

    @Test
    fun `report reviewer can get resource hashes for unpublished report`()
    {
        insertReport("test", "version1", published = false)
        insertFileInput("version1", "testfile.csv", FilePurpose.RESOURCE)
        val sut = createSut(isReviewer = true)

        sut.getResourceHashes("test", "version1")
    }
}
