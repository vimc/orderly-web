package org.vaccineimpact.orderlyweb

import org.vaccineimpact.orderlyweb.errors.UnsupportedValueException
import spark.*
import spark.route.HttpMethod

// Make classes which use the static methods of Spark unit testable by providing a wrapper interface for them to use
// instead
interface SparkWrapper
{
    fun before(path: String, acceptType: String, method: HttpMethod, filter: Filter)
    fun after(path: String, acceptType: String, vararg filters: Filter)
    fun map(
            fullUrl: String,
            method: HttpMethod,
            acceptType: String,
            route: Route,
            transformer: ResponseTransformer? = null
    )
    fun mapGet(fullUrl: String, route: Route)
}

class SparkServiceWrapper : SparkWrapper
{
    override fun mapGet(fullUrl: String, route: Route)
    {
        Spark.get(fullUrl, route)
    }

    override fun before(path: String, acceptType: String, method: HttpMethod, filter: Filter)
    {
        val methodMatchingFilter = MethodMatchingFilter(method, filter)
        Spark.before(path, acceptType, methodMatchingFilter)
    }

    override fun after(path: String, acceptType: String, vararg filters: Filter)
    {
        Spark.after(path, acceptType, *filters)
    }

    override fun map(fullUrl: String, method: HttpMethod, acceptType: String, route: Route,
                     transformer: ResponseTransformer?)
    {
        when (transformer)
        {
            null -> when (method)
            {
                HttpMethod.get -> Spark.get(fullUrl, acceptType, route)
                HttpMethod.post -> Spark.post(fullUrl, acceptType, route)
                HttpMethod.put -> Spark.put(fullUrl, acceptType, route)
                HttpMethod.patch -> Spark.patch(fullUrl, acceptType, route)
                HttpMethod.delete -> Spark.delete(fullUrl, acceptType, route)
                else -> throw UnsupportedValueException(method)
            }
            else ->
                when (method)
                {
                    HttpMethod.get -> Spark.get(fullUrl, acceptType, route, transformer)
                    HttpMethod.post -> Spark.post(fullUrl, acceptType, route, transformer)
                    HttpMethod.put -> Spark.put(fullUrl, acceptType, route, transformer)
                    HttpMethod.patch -> Spark.patch(fullUrl, acceptType, route, transformer)
                    HttpMethod.delete -> Spark.delete(fullUrl, acceptType, route, transformer)
                    else -> throw UnsupportedValueException(method)
                }
        }
    }
}

class MethodMatchingFilter(val method: HttpMethod,
                           val filter: Filter) : Filter {

    override fun handle(request: Request?, response: Response?)
    {
        if (request?.requestMethod()?.lowercase() == method.toString().lowercase()) {
            return filter.handle(request, response)
        }
    }
}
