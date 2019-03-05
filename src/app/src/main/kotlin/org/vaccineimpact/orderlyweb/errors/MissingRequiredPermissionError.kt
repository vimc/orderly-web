package org.vaccineimpact.orderlyweb.errors

import org.vaccineimpact.orderlyweb.models.ErrorInfo
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission

class MissingRequiredPermissionError(missingPermissions: Set<ReifiedPermission>) : MontaguError(403, listOf(
        ErrorInfo("forbidden", "You do not have sufficient permissions to access this resource. " +
                "Missing these permissions: ${missingPermissions.joinToString(", ")}")
))