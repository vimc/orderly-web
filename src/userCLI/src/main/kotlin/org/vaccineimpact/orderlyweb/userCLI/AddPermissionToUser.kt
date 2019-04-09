package org.vaccineimpact.orderlyweb.userCLI

import org.vaccineimpact.orderlyweb.db.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission

fun grantPermissions(options: Map<String, Any>)
{
    val userGroup = options["<group>"].toString()
    val permissions = options["<permission>"] as List<*>
    try
    {
        permissions.map {
            val permission = it.toString()
            OrderlyAuthorizationRepository().ensureUserGroupHasPermission(userGroup,
                    ReifiedPermission.parse(permission))
            println("Gave user group '$userGroup' the permission '$permission'")
        }

    }
    catch (e: Exception)
    {
        println("An error occurred saving permissions to the database:")
        println(e)
    }
}
