package org.vaccineimpact.orderlyweb.test_helpers

import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables
import java.sql.Timestamp

fun insertReport(name: String,
                 version: String,
                 published: Boolean = true,
                 date: Timestamp = Timestamp(System.currentTimeMillis()),
                 author: String = "author authorson",
                 requester: String = "requester mcfunder")
{

    JooqContext().use {

        val displayname = "display name $name"

        //Does the report already exist in the REPORT table?
        val rows = it.dsl.select(Tables.REPORT.NAME)
                .from(Tables.REPORT)
                .where(Tables.REPORT.NAME.eq(name))
                .fetch()

        if (rows.isEmpty())
        {

            val reportRecord = it.dsl.newRecord(Tables.REPORT)
                    .apply {
                        this.name = name
                    }
            reportRecord.store()
        }

        val reportVersionRecord = it.dsl.newRecord(Tables.REPORT_VERSION)
                .apply {
                    this.id = version
                    this.report = name
                    this.date = date
                    this.displayname = displayname
                    this.description = "description $name"
                    this.requester = requester
                    this.author = author
                    this.published = published
                    this.connection = false
                }
        reportVersionRecord.store()

        //Update latest version of Report
        it.dsl.update(Tables.REPORT)
                .set(Tables.REPORT.LATEST, version)
                .where(Tables.REPORT.NAME.eq(name))
                .execute()

    }

}

