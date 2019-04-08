package org.vaccineimpact.orderlyweb.userCLI

import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.db.Tables
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission

fun addPermissionToGroup(options: Map<String, Any>)
{
    val userGroup = options["<group>"].toString()
    val permission = options["<permission>"].toString()
    try
    {
        if (!userGroupExists(userGroup))
        {
            println("User group with name '$userGroup' does not exist; no changes made")
        }
        else
        {
            OrderlyAuthorizationRepository().ensureUserGroupHasPermission(userGroup,
                    ReifiedPermission.parse(permission))
            println("Gave user group with name '$userGroup' the permission '$permission'")
        }
    }
    catch (e: Exception)
    {
        println("An error occurred saving the permission to the database:")
        println(e)
    }
}

private fun userGroupExists(id: String): Boolean
{
    JooqContext().use {
        return it.dsl.fetchOne(Tables.ORDERLYWEB_USER_GROUP, Tables.ORDERLYWEB_USER_GROUP.ID.eq(id)) != null
    }
}


