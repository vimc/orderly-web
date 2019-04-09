package org.vaccineimpact.orderlyweb.userCLI

import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.db.Tables
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission

fun grantPermissions(options: Map<String, Any>)
{
    val userGroup = options["<group>"].toString()
    val permissions = options["<permission>"] as List<*>
    try
    {
        if (!userGroupExists(userGroup))
        {
            println("User group with name '$userGroup' does not exist; no changes made")
        }
        else
        {
            permissions.map {
                val permission = it.toString()
                OrderlyAuthorizationRepository().ensureUserGroupHasPermission(userGroup,
                        ReifiedPermission.parse(permission))
                println("Gave user group '$userGroup' the permission '$permission'")
            }
        }

    }
    catch (e: Exception)
    {
        println("An error occurred saving permissions to the database:")
        println(e)
    }
}

private fun userGroupExists(id: String): Boolean
{
    JooqContext().use {
        return it.dsl.fetchOne(Tables.ORDERLYWEB_USER_GROUP, Tables.ORDERLYWEB_USER_GROUP.ID.eq(id)) != null
    }
}


