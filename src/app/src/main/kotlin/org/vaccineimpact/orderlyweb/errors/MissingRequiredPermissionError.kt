package org.vaccineimpact.orderlyweb.errors

import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission

class MissingRequiredPermissionError(missingPermissions: Set<ReifiedPermission>) : OrderlyWebError(403, listOf(
        org.vaccineimpact.orderlyweb.models.ErrorInfo("forbidden", "You do not have sufficient permissions to access this resource. " +
                "Missing these permissions: ${missingPermissions.joinToString(", ")}")
))