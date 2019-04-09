package org.vaccineimpact.orderlyweb.userCLI

import org.vaccineimpact.orderlyweb.db.OrderlyAuthorizationRepository

fun addUserGroup(options: Map<String, Any>)
{
    val groupName = options["<name>"].toString()
    try
    {
        OrderlyAuthorizationRepository().createUserGroup(groupName)
        println("Saved user group '$groupName' to the database")
    }
    catch (e: Exception)
    {
        println("An error occurred saving the user group to the database:")
        println(e)
    }
}
