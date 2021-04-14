package org.vaccineimpact.orderlyweb.tests.unit_tests.templates.VersionPage

import org.vaccineimpact.orderlyweb.models.*
import org.vaccineimpact.orderlyweb.viewmodels.*
import java.sql.Timestamp

object VersionPageTestData
{
    val testBasicReportVersion = ReportVersionWithDescLatestElapsed(name = "r1",
            displayName = "r1 display",
            id = "r1-v1",
            published = true,
            date = Timestamp(System.currentTimeMillis()).toInstant(),
            latestVersion = "v1",
            description = "r1 description",
            elapsed = 1.5,
            gitBranch="master",
            gitCommit="abc123")

    val testReport = ReportVersionWithArtefactsDataDescParamsResources(testBasicReportVersion,
            artefacts = listOf(),
            resources = listOf(),
            dataInfo = listOf(),
            parameterValues = mapOf("p1" to "v1", "p2" to "v2"))

    val testArtefactViewModels = listOf(
            ArtefactViewModel(
                    Artefact(ArtefactFormat.DATA, "artefact1", listOf()),
                    listOf(
                            DownloadableFileViewModel("a1file1.png", "http://a1file1", 19876),
                            DownloadableFileViewModel("a1file2.pdf", "http://a1file2", 123)
                    ),
                    "inlinesrc.png"
            ),
            ArtefactViewModel(
                    Artefact(ArtefactFormat.DATA, "artefact2", listOf()),
                    listOf(
                            DownloadableFileViewModel("a2file1.xls", "http://a2file1", 2300000)
                    ),
                    null
            )
    )

    val testDataLinks = listOf(
            InputDataViewModel("key1",
                    DownloadableFileViewModel("key1.csv", "http://key1/csv", 1720394),
                    DownloadableFileViewModel("key1.rds", "http://key1/rds", 4123451)),
            InputDataViewModel("key2",
                    DownloadableFileViewModel("key2.csv", "http://key2/csv", 3123),
                    DownloadableFileViewModel("key2.rds", "http://key2/rds", 4562))
    )

    val testResources = listOf(
            DownloadableFileViewModel("resource1.csv", "http://resource1/csv", 1234),
            DownloadableFileViewModel("resource2.csv", "http://resource2/csv", 2345)
    )

    val testDefaultModel = DefaultViewModel(true, "username",
            isReviewer = false,
            isAdmin = false,
            isGuest = false,
            breadcrumbs = listOf(Breadcrumb("name", "url")))

    val testModel = ReportVersionPageViewModel(
                testReport.basicReportVersion,
                "/testFocalArtefactUrl",
                false,
                testArtefactViewModels,
                testDataLinks,
                testResources,
                DownloadableFileViewModel("zipFileName", "http://zipFileUrl", null),
                listOf(),
                listOf(),
                "p1=v1, p2=v2",
                "Mon 12 Jun 2020 14:23",
                "3 hours 2 minutes",
                testDefaultModel)
}
