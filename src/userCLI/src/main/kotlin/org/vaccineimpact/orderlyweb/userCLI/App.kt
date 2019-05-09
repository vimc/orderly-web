package org.vaccineimpact.orderlyweb.userCLI

import org.docopt.Docopt
import kotlin.system.exitProcess

const val doc = """
OrderlyWeb User CLI

Usage:
    app add-users <email>...
    app add-groups <name>...
    app add-members <group> <email>...
    app grant <group> <permission>...
"""

fun main(args: Array<String>)
{
    val options = Docopt(doc).parse(args.toList())
    val addUser = options["add-users"] as Boolean
    val addGroup = options["add-groups"] as Boolean
    val addMembers = options["add-members"] as Boolean
    val grant = options["grant"] as Boolean
    try
    {
        val result = when
        {
            addUser -> addUsers(options)
            grant -> grantPermissions(options)
            addGroup -> addUserGroups(options)
            addMembers -> addMembers(options)
            else -> ""
        }

        print(result)
    }
    catch (e: Exception)
    {
        System.err.println(e.message)
        exitProcess(1)
    }
}

fun Any?.getStringValue(): String
{
    return this.toString().replace("[", "").replace("]","")
}