package org.vaccineimpact.orderlyweb.db

import java.io.FileNotFoundException
import java.net.URL

fun getResource(path: String): URL
{
    val url: URL? = AppConfig::class.java.classLoader.getResource(path)
    if (url != null)
    {
        return url
    }
    else
    {
        throw FileNotFoundException("Unable to load '$path' as a resource steam")
    }
}