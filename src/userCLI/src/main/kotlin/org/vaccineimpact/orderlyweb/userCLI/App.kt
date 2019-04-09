package org.vaccineimpact.orderlyweb.userCLI

import org.docopt.Docopt
import kotlin.system.exitProcess

const val doc = """
OrderlyWeb User CLI

Usage:
    app add-user <email>
    app add-group <name>
    app grant <group> <permission>...
"""

fun main(args: Array<String>)
{
    val options = Docopt(doc).parse(args.toList())
    val addUser = options["add-user"] as Boolean
    val addGroup = options["add-group"] as Boolean
    val grant = options["grant"] as Boolean
    try
    {
        val result = when
        {
            addUser -> addUser(options)
            grant -> grantPermissions(options)
            addGroup -> addGroup()
            else -> ""
        }

        print(result)
    }
    catch (e: Exception)
    {
        println(e.message)
        exitProcess(-1)
    }
}

fun addGroup(): String
{
    return "Coming soon!"
}