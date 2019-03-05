package org.vaccineimpact.orderlyweb.errors

import org.vaccineimpact.api.models.permissions.ReifiedPermission

class MissingRequiredPermissionError(missingPermissions: Set<ReifiedPermission>) : MontaguError(403, listOf(
        org.vaccineimpact.api.models.ErrorInfo("forbidden", "You do not have sufficient permissions to access this resource. " +
                "Missing these permissions: ${missingPermissions.joinToString(", ")}")
))