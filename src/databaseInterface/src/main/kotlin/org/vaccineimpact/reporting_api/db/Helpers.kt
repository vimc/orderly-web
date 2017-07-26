package org.vaccineimpact.reporting_api.db

import java.io.FileNotFoundException
import java.net.URL

fun getResource(path: String): URL {
    val url: URL? = Config::class.java.classLoader.getResource(path)
    if (url != null) {
        return url
    } else {
        throw FileNotFoundException("Unable to load '$path' as a resource steam")
    }
}