package org.vaccineimpact.orderlyweb

import okhttp3.OkHttpClient
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.errors.PorcelainError
import org.vaccineimpact.orderlyweb.models.Parameter

interface OrderlyServerAPI : PorcelainAPI
{
    @Throws(PorcelainError::class)
    fun getRunnableReportNames(queryParams: Map<String, String>): List<String>

    @Throws(PorcelainError::class)
    fun getReportParameters(reportName: String, queryParams: Map<String, String>): List<Parameter>

    override fun throwOnError(): OrderlyServerAPI
}

class OrderlyServerClient(
        config: Config,
        client: OkHttpClient = OkHttpClient()
) : OrderlyServerAPI, PorcelainAPIServer("Orderly server", config["orderly.server"], client)
{
    override fun getRunnableReportNames(queryParams: Map<String, String>): List<String>
    {
        return get("/reports/source", queryParams).listData(String::class.java)
    }

    override fun getReportParameters(reportName: String, queryParams: Map<String, String>): List<Parameter>
    {
        return get("/reports/$reportName/parameters", queryParams).listData(Parameter::class.java)
    }

    override fun throwOnError(): OrderlyServerAPI
    {
        return apply {
            throwOnError = true
        }
    }
}
