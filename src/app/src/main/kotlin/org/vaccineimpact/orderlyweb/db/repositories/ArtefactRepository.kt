package org.vaccineimpact.orderlyweb.db.repositories

import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError

interface ArtefactRepository
{
    @Throws(UnknownObjectError::class)
    fun getArtefactHashes(name: String, version: String): Map<String, String>

    @Throws(UnknownObjectError::class)
    fun getArtefactHash(name: String, version: String, filename: String): String

}

class OrderlyArtefactRepository : ArtefactRepository
{
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
