package org.vaccineimpact.orderlyweb

import spark.Filter
import spark.Spark

//Make classes which use the static methods of Spark unit testable by providing a wrapper interface for them to use instead
interface SparkWrapper {
    fun before(path: String, filter: Filter)
    fun after(path: String, acceptType: String, vararg filters: Filter)
}

class SparkServiceWrapper : SparkWrapper {
    override fun before(path: String, filter: Filter)
    {
        Spark.before(path, filter)
    }

    override fun after(path: String, acceptType: String, vararg filters: Filter)
    {
        Spark.after(path, acceptType, *filters)
    }

}