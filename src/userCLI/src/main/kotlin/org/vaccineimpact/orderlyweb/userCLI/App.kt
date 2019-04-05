package org.vaccineimpact.orderlyweb.userCLI

import org.docopt.Docopt
import kotlin.system.exitProcess

const val doc = """
OrderlyWeb User CLI

Usage:
    app add-user <email>
    app add-permission <user> <permission>...
"""

fun main(args: Array<String>)
{
    val options = Docopt(doc).parse(args.toList())
    val addUser = options["add-user"] as Boolean
    val addPermission = options["add-permission"] as Boolean
    try
    {
        if (addUser)
        {
            addUser(options)
        }
        else if (addPermission)
        {
            addPermission(options)
        }
    }
    catch (e: Exception)
    {
        println(e.message)
        exitProcess(-1)
    }
}

fun addPermission(options: Map<String, Any>)
{
    println("Coming soon!")
}