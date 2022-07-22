package org.vaccineimpact.orderlyweb.controllers.api

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.errors.OrderlyFileNotFoundError

class DataController(context: ActionContext,
                     private val orderly: OrderlyClient,
                     private val files: FileSystem,
                     private val config: Config) : Controller(context)
{
    constructor(context: ActionContext) :
            this(context,
                    Orderly(context),
                    Files(),
                    AppConfig())

    fun get(): Map<String, String>
    {
        return orderly.getData(context.params(":name"), context.params(":version"))
    }

    fun downloadCSV(): Boolean
    {
        val id = context.params(":id")
        val absoluteFilePath = "${this.config["orderly.root"]}data/csv/$id.csv"

        return downloadFile(files, absoluteFilePath, "$id.csv", ContentTypes.csv)
    }

    fun downloadRDS(): Boolean
    {
        val id = context.params(":id")
        val absoluteFilePath = "${this.config["orderly.root"]}data/rds/$id.rds"

        return downloadFile(files, absoluteFilePath, "$id.rds", ContentTypes.binarydata)
    }

    fun downloadData(): Boolean
    {
        val name = context.params(":name")
        val version = context.params(":version")
        val id = context.params(":data")
        var type = context.queryParams("type")

        if (type.isNullOrEmpty())
        {
            type = "csv"
        }

        val hash = orderly.getDatum(name, version, id)

        val absoluteFilePath = "${this.config["orderly.root"]}data/$type/$hash.$type"

        val contentType =
                if (type == "csv")
                {
                    ContentTypes.csv
                }
                else
                {
                    ContentTypes.binarydata
                }

        return downloadFile(files, absoluteFilePath, "$hash.$type", contentType)
    }
}
