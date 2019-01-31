package org.vaccineimpact.reporting_api.tests.database_tests

import org.junit.Test
import org.junit.Ignore
import org.assertj.core.api.Assertions.assertThat

import org.vaccineimpact.reporting_api.db.JooqContext
import org.vaccineimpact.reporting_api.db.Tables.ARTEFACT_FORMAT
import org.vaccineimpact.reporting_api.db.Tables.CHANGELOG
import org.vaccineimpact.reporting_api.db.Tables.CHANGELOG_LABEL
import org.vaccineimpact.reporting_api.db.Tables.DATA
import org.vaccineimpact.reporting_api.db.Tables.DEPENDS
import org.vaccineimpact.reporting_api.db.Tables.FILE
import org.vaccineimpact.reporting_api.db.Tables.FILE_ARTEFACT
import org.vaccineimpact.reporting_api.db.Tables.FILE_INPUT
import org.vaccineimpact.reporting_api.db.Tables.FILE_PURPOSE
import org.vaccineimpact.reporting_api.db.Tables.ORDERLY_SCHEMA
import org.vaccineimpact.reporting_api.db.Tables.ORDERLY_SCHEMA_TABLES
import org.vaccineimpact.reporting_api.db.Tables.REPORT
import org.vaccineimpact.reporting_api.db.Tables.REPORT_VERSION
import org.vaccineimpact.reporting_api.db.Tables.REPORT_VERSION_ARTEFACT
import org.vaccineimpact.reporting_api.db.Tables.REPORT_VERSION_DATA
import org.vaccineimpact.reporting_api.db.Tables.REPORT_VERSION_PACKAGE


class OrderlyDemoDBTests : DatabaseTests()
{
    //NB These tests hit the database directly to check that expected Demo data is present, because the Orderly
    //class is not yet querying all the new tables. We do not not check the old Orderly table, which will shortly be
    //deprecated

    @Test
    fun `has expected artefact formats`()
    {
        JooqContext().use {
            val result = it.dsl.selectFrom(ARTEFACT_FORMAT)
                    .orderBy(ARTEFACT_FORMAT.NAME.asc())
                    .fetch()

            val names = result.map({it.name})
            assertThat(names.contains("staticgraph")).isTrue()
            assertThat(names.contains("interactivegraph")).isTrue()
            assertThat(names.contains("data")).isTrue()
            assertThat(names.contains("report")).isTrue()
            assertThat(names.contains("interactivehtml")).isTrue()

        }
    }

    @Test
    fun `has expected changelog`()
    {
        JooqContext().use {
            val result = it.dsl.selectFrom(CHANGELOG)
                    .orderBy(CHANGELOG.VALUE.asc())
                    .fetch()

            assertThat(result[0].value.startsWith("Do you see any Teletubbies in here?")).isTrue()
            assertThat(result[0].label).isEqualTo("public")
            assertThat(result[0].fromFile).isTrue()
            assertThat(result[0].reportVersion).isNotBlank()

        }
    }

    @Test
    fun `has expected changelog labels`()
    {
        JooqContext().use {
            val result = it.dsl.selectFrom(CHANGELOG_LABEL)
                    .orderBy(CHANGELOG_LABEL.ID.asc())
                    .fetch()

            assertThat(result[0].id).isEqualTo("internal")
            assertThat(result[0].public).isFalse()

            assertThat(result[1].id).isEqualTo("public")
            assertThat(result[1].public).isTrue()

        }
    }

    @Test
    fun `has populated data table`()
    {
        JooqContext().use {
            val result = it.dsl.selectFrom(DATA)
                    .fetch()

            assertThat(result.count()).isGreaterThan(0)

            for (r in result)
            {
                assertThat(r.hash).isNotBlank()
                assertThat(r.sizeCsv).isGreaterThan(0)
                assertThat(r.sizeRds).isGreaterThan(0)
            }

        }
    }

    @Test
    fun `has populated depends table`()
    {
        JooqContext().use {
            val result = it.dsl.selectFrom(DEPENDS)
                    .fetch()

            assertThat(result.count()).isGreaterThan(0)

            for (r in result)
            {
                assertThat(r.id).isGreaterThan(0)
                assertThat(r.reportVersion).isNotBlank()
                assertThat(r.use).isGreaterThan(0)
                assertThat(r.`as`).isNotBlank()
                assertThat(r.isPinned).isNotNull()
                assertThat(r.isLatest).isNotNull()
            }

        }
    }

    @Test
    fun `has populated file table`()
    {
        JooqContext().use {
            val result = it.dsl.selectFrom(FILE)
                    .fetch()

            assertThat(result.count()).isGreaterThan(0)

            for (r in result)
            {
                assertThat(r.hash).isNotBlank()
                assertThat(r.size).isNotNull()
            }

        }
    }

    @Test
    fun `has populated file artefact table`()
    {
        JooqContext().use {
            val result = it.dsl.selectFrom(FILE_ARTEFACT)
                    .fetch()

            assertThat(result.count()).isGreaterThan(0)

            for (r in result)
            {
                assertThat(r.id).isGreaterThan(0)
                assertThat(r.artefact).isGreaterThan(0)
                assertThat(r.fileHash).isNotBlank()
                assertThat(r.filename).isNotBlank()
            }

        }
    }

    @Test
    fun `has populated file input table`()
    {
        JooqContext().use {
            val result = it.dsl.selectFrom(FILE_INPUT)
                    .fetch()

            assertThat(result.count()).isGreaterThan(0)

            for (r in result)
            {
                assertThat(r.id).isGreaterThan(0)
                assertThat(r.reportVersion).isNotBlank()
                assertThat(r.fileHash).isNotBlank()
                assertThat(r.filename).isNotBlank()
                assertThat(r.filePurpose).isNotBlank()
            }

        }
    }

    @Test
    fun `has expected file purposes`()
    {
        JooqContext().use {
            val result = it.dsl.selectFrom(FILE_PURPOSE)
                    .fetch()

            val names = result.map({it.name})
            assertThat(names.contains("source")).isTrue()
            assertThat(names.contains("script")).isTrue()
            assertThat(names.contains("resource")).isTrue()
            assertThat(names.contains("orderly_yml")).isTrue()

        }
    }

    @Test
    @Ignore //This fails at the moment because created Timestamp is stored as a number, which Jooq can't deal with
    fun `has populated orderly schema`()
    {
        JooqContext().use {
            val result = it.dsl.selectFrom(ORDERLY_SCHEMA)
                    .fetchOne()

            assertThat(result.schemaVersion.isNotBlank())
            assertThat(result.orderlyVersion.isNotBlank())
            assertThat(result.created.getTime()).isGreaterThan(0)

        }
    }

    @Test
    fun `has populated orderly schema tables`()
    {
        JooqContext().use {
            val result = it.dsl.selectFrom(ORDERLY_SCHEMA_TABLES)
                    .fetch()

            val names = result.map({it.name})
            assertThat(names.contains("artefact_format")).isTrue()
            assertThat(names.contains("changelog")).isTrue()
            assertThat(names.contains("changelog_label")).isTrue()
            assertThat(names.contains("data")).isTrue()
            assertThat(names.contains("depends")).isTrue()
            assertThat(names.contains("file")).isTrue()
            assertThat(names.contains("file_artefact")).isTrue()
            assertThat(names.contains("file_input")).isTrue()
            assertThat(names.contains("file_purpose")).isTrue()
            assertThat(names.contains("orderly_schema")).isTrue()
            assertThat(names.contains("orderly_schema_tables")).isTrue()
            assertThat(names.contains("report")).isTrue()
            assertThat(names.contains("report_version")).isTrue()
            assertThat(names.contains("report_version_artefact")).isTrue()
            assertThat(names.contains("report_version_data")).isTrue()
            assertThat(names.contains("report_version_package")).isTrue()
            assertThat(names.contains("report_version_view")).isTrue()

        }
    }

    @Test
    fun `has expected reports`()
    {
        JooqContext().use {
            val result = it.dsl.selectFrom(REPORT)
                    .orderBy(REPORT.NAME.asc())
                    .fetch()

            val names = result.map({it.name})
            assertThat(names.contains("changelog")).isTrue()
            assertThat(names.contains("connection")).isTrue()
            assertThat(names.contains("html")).isTrue()
            assertThat(names.contains("interactive")).isTrue()
            assertThat(names.contains("minimal")).isTrue()
            assertThat(names.contains("other")).isTrue()

        }
    }

    @Test
    fun `has populated report versions`()
    {
        JooqContext().use {
            val result = it.dsl.selectFrom(REPORT_VERSION)
                    .fetch()

            assertThat(result.count()).isGreaterThan(0)

            var atLeastOneDisplayName = false
            var atLeastOneDescription = false
            var atLeastOneComment = false

            for (r in result)
            {
                assertThat(r.id).isNotBlank()
                assertThat(r.report).isNotBlank()
                assertThat(r.date.getTime()).isGreaterThan(0)
                assertThat(r.connection).isNotNull()
                assertThat(r.published).isNotNull()
                assertThat(r.requester).isNotBlank()
                assertThat(r.author).isNotBlank()


                if (r.displayname?.isNotBlank()?:false) atLeastOneDisplayName = true
                if (r.description?.isNotBlank()?:false) atLeastOneDescription = true
                if (r.comment?.isNotBlank()?:false) atLeastOneComment = true

            }

            assertThat(atLeastOneDisplayName)
            assertThat(atLeastOneDescription)
            assertThat(atLeastOneComment)
        }
    }


    @Test
    fun `has populated report versions artefacts`()
    {
        JooqContext().use {
            val result = it.dsl.selectFrom(REPORT_VERSION_ARTEFACT)
                    .fetch()

            assertThat(result.count()).isGreaterThan(0)

            for (r in result)
            {
                assertThat(r.id).isGreaterThan(0)
                assertThat(r.reportVersion).isNotBlank()
                assertThat(r.format).isNotBlank()
                assertThat(r.description).isNotBlank()
                assertThat(r.order).isGreaterThan(0)
            }

        }
    }

    @Test
    fun `has populated report version data`()
    {
        JooqContext().use {
            val result = it.dsl.selectFrom(REPORT_VERSION_DATA)
                    .fetch()

            assertThat(result.count()).isGreaterThan(0)

            for (r in result)
            {
                assertThat(r.id).isGreaterThan(0)
                assertThat(r.reportVersion).isNotBlank()
                assertThat(r.name).isNotBlank()
                assertThat(r.sql).isNotBlank()
                assertThat(r.hash).isNotBlank()
            }

        }
    }

    @Test
    fun `has populated report version package`()
    {
        JooqContext().use {
            val result = it.dsl.selectFrom(REPORT_VERSION_PACKAGE)
                    .fetch()

            assertThat(result.count()).isGreaterThan(0)

            for (r in result)
            {
                assertThat(r.id).isGreaterThan(0)
                assertThat(r.reportVersion).isNotBlank()
                assertThat(r.packageName).isNotBlank()
                assertThat(r.packageVersion).isNotBlank()
            }

        }
    }


}