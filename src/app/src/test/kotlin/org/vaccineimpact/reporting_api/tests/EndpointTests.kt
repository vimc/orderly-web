package org.vaccineimpact.reporting_api.tests

import org.junit.Test
import com.nhaarman.mockito_kotlin.mock
import org.vaccineimpact.reporting_api.controllers.Endpoint
import org.vaccineimpact.reporting_api.controllers.getWrappedRoute
import spark.Request
import spark.Response

class EndpointTests
{

    @Test
    fun getWrappedRoute()
    {
        val ep = Endpoint("/reports/", "Report", "getAll")
        val route = ep.getWrappedRoute()
        route.handle(mock<Request>(), mock<Response>())
    }

}
