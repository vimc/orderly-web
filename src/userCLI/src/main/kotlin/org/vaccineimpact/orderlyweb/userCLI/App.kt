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
    val addPermissions = options["grant"] as Boolean
    try
    {
        if (addUser)
        {
            addUser(options)
        }
        else if (addPermissions)
        {
            grantPermissions(options)
        }
        else if (addGroup)
        {
            addGroup()
        }
    }
    catch (e: Exception)
    {
        println(e.message)
        exitProcess(-1)
    }
}

fun addGroup()
{
    println("Coming soon!")
}