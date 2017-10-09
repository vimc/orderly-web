package org.vaccineimpact.reporting_api.db

import java.io.File
import java.util.*

interface Config
{
    operator fun get(key: String): String
}

object AppConfig : Config
{
    private val properties = Properties().apply {
        load(getResource("config.properties").openStream())
        val global = File("/etc/montagu/reporting-reporting_api/config.properties")
        if (global.exists())
        {
            global.inputStream().use { load(it) }
        }
    }

    operator override fun get(key: String): String
    {
        val x = properties[key]
        if (x != null)
        {
            val value = x as String
            if (value.startsWith("\${"))
            {
                throw MissingConfiguration(key)
            }
            return value
        }
        else
        {
            throw MissingConfigurationKey(key)
        }
    }

    fun getInt(key: String) = get(key).toInt()
}

class MissingConfiguration(key: String) : Exception("Detected a value like \${foo} for key '$key' in the configuration. This probably means that the config template has not been processed. Try running ./gradlew :PROJECT:copy[Test]Config")
class MissingConfigurationKey(val key: String) : Exception("Missing configuration key '$key'")