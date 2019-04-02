package org.vaccineimpact.orderlyweb.userCLI

import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.security.authorization.OrderlyAuthorizationRepository
import kotlin.system.exitProcess

class AddPermissionOptions(
        val userGroup: String,
        val permissionName: String,
        val scope: Scope
)
{
    companion object
    {
        fun parseArgs(args: List<String>): AddPermissionOptions
        {
            if (args.size != 2 && args.size != 3)
            {
                println("Usage: ./user.sh addPermission USERGROUP PERMISSION_NAME [SCOPE_PREFIX:SCOPE_ID]")
                println("For global permissions, leave off the last two arguments")
                exitProcess(0)
            }

            val username = args[0]
            val permissionName = args[1]
            var scope: Scope = Scope.Global()
            if (args.size == 3)
            {
                scope = Scope.parse(args[2])
            }
            return AddPermissionOptions(username, permissionName, scope)
        }

    }
}

fun addPermission(args: List<String>)
{
    AddPermissionOptions.parseArgs(args).run {
        OrderlyAuthorizationRepository().ensureUserGroupHasPermission(userGroup, ReifiedPermission(permissionName, scope))
        println("Gave permission $scope/$permissionName to group '$userGroup'")
    }
}

