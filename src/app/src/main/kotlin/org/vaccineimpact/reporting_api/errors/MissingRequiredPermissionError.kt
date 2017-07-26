package org.vaccineimpact.reporting_api.errors

class MissingRequiredPermissionError(missingPermissions: Set<String>) : MontaguError(403, listOf(
        org.vaccineimpact.api.models.ErrorInfo("forbidden", "You do not have sufficient permissions to access this resource. " +
                "Missing these permissions: ${missingPermissions.joinToString(", ")}")
))