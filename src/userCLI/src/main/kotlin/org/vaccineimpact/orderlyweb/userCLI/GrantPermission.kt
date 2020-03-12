package org.vaccineimpact.orderlyweb.userCLI

import org.vaccineimpact.orderlyweb.db.repositories.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission

fun grantPermissions(options: Map<String, Any>): String
{
    val userGroup = options["<group>"].getStringValue()
    val permissions = options["<permission>"] as List<*>
    return permissions.joinToString("\n") {
        val permission = it.getStringValue()
        OrderlyAuthorizationRepository().ensureUserGroupHasPermission(userGroup,
                ReifiedPermission.parse(permission))

        "Gave user group '$userGroup' the permission '$permission'"
    }

}
