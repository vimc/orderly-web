package org.vaccineimpact.orderlyweb.db.repositories

import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables
import org.vaccineimpact.orderlyweb.db.parseEnum
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.models.Artefact
import org.vaccineimpact.orderlyweb.models.FileInfo

interface ArtefactRepository
{
    @Throws(UnknownObjectError::class)
    fun getArtefactHashes(name: String, version: String): Map<String, String>

    @Throws(UnknownObjectError::class)
    fun getArtefacts(report: String, version: String): List<Artefact>

    @Throws(UnknownObjectError::class)
    fun getArtefactHash(name: String, version: String, filename: String): String
}

class OrderlyArtefactRepository : ArtefactRepository
{
    override fun getArtefacts(report: String, version: String): List<Artefact>
    {
        JooqContext().use {

            return it.dsl.select(Tables.REPORT_VERSION_ARTEFACT.ID, Tables.REPORT_VERSION_ARTEFACT.FORMAT,
                    Tables.REPORT_VERSION_ARTEFACT.DESCRIPTION)
                    .from(Tables.REPORT_VERSION_ARTEFACT)
                    .where(Tables.REPORT_VERSION_ARTEFACT.REPORT_VERSION.eq(version))
                    .fetch()
                    .map { a ->
                        val id = a[Tables.REPORT_VERSION_ARTEFACT.ID]
                        val format = a[Tables.REPORT_VERSION_ARTEFACT.FORMAT]
                        val description = a[Tables.REPORT_VERSION_ARTEFACT.DESCRIPTION]
                        val files = it.dsl.select(Tables.FILE_ARTEFACT.FILENAME, Tables.FILE.SIZE)
                                .from(Tables.FILE_ARTEFACT)
                                .innerJoin(Tables.FILE)
                                .on(Tables.FILE_ARTEFACT.FILE_HASH.eq(Tables.FILE.HASH))
                                .where(Tables.FILE_ARTEFACT.ARTEFACT.eq(id))
                                .fetch()
                                .map { r -> FileInfo(r[Tables.FILE_ARTEFACT.FILENAME], r[Tables.FILE.SIZE]) }

                        Artefact(parseEnum(format), description, files)
                    }
        }
    }

    override fun getArtefactHashes(name: String, version: String): Map<String, String>
    {
        return JooqContext().use { ctx ->

            ctx.dsl.select(Tables.FILE_ARTEFACT.FILENAME, Tables.FILE_ARTEFACT.FILE_HASH)
                    .from(Tables.FILE_ARTEFACT)
                    .join(Tables.REPORT_VERSION_ARTEFACT)
                    .on(Tables.FILE_ARTEFACT.ARTEFACT.eq(Tables.REPORT_VERSION_ARTEFACT.ID))
                    .where(Tables.REPORT_VERSION_ARTEFACT.REPORT_VERSION.eq(version))
                    .fetch()
                    .associate { it[Tables.FILE_ARTEFACT.FILENAME] to it[Tables.FILE_ARTEFACT.FILE_HASH] }
        }
    }

    override fun getArtefactHash(name: String, version: String, filename: String): String
    {
        return getArtefactHashes(name, version)[filename]
                ?: throw UnknownObjectError(filename, "Artefact")
    }
}
