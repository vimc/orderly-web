package org.vaccineimpact.orderlyweb.userCLI

import org.docopt.Docopt
import kotlin.system.exitProcess

const val doc = """
OrderlyWeb User CLI

Usage:
    app add-user <email>
    app add-group <name>
    app add-members <group> <email>...
    app grant <group> <permission>...
"""

fun main(args: Array<String>)
{
    val options = Docopt(doc).parse(args.toList())
    val addUser = options["add-user"] as Boolean
    val addGroup = options["add-group"] as Boolean
    val addMembers = options["add-members"] as Boolean
    val grant = options["grant"] as Boolean
    try
    {
        val result = when
        {
            addUser -> addUser(options)
            grant -> grantPermissions(options)
            addGroup -> addUserGroup(options)
            addMembers -> addMembers(options)
            else -> ""
        }

        print(result)
    }
    catch (e: Exception)
    {
        println(e.message)
        exitProcess(1)
    }
}