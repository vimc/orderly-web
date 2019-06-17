package org.vaccineimpact.orderlyweb.db

import java.io.File
import java.util.*

interface Config
{
    operator fun get(key: String): String

    val authorizationEnabled: Boolean
}

class AppConfig : Config
{
    companion object
    {
        val properties = Properties().apply {
            load(getResource("config.properties").openStream())
            val global = File("/etc/orderly/web/config.properties")
            if (global.exists())
            {
                global.inputStream().use { load(it) }
            }
        }
    }

    override operator fun get(key: String): String
    {
        val x = AppConfig.properties[key]
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

    override val authorizationEnabled by lazy {
        getBool("auth.fine_grained")
    }
    
    fun getInt(key: String) = get(key).toInt()
    fun getBool(key: String) = get(key).toBoolean()
}

class MissingConfiguration(key: String) : Exception("Detected a value like \${foo} for key '$key' in the configuration. This probably means that the config template has not been processed. Try running ./gradlew :PROJECT:copy[Test]Config")
class MissingConfigurationKey(val key: String) : Exception("Missing configuration key '$key'")