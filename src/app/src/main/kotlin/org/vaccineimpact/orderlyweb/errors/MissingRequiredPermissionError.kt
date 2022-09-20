package org.vaccineimpact.orderlyweb.errors

import org.eclipse.jetty.http.HttpStatus
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission

class MissingRequiredPermissionError(missingPermissions: Set<ReifiedPermission>) : OrderlyWebError(
        HttpStatus.FORBIDDEN_403,
        listOf(
                org.vaccineimpact.orderlyweb.models.ErrorInfo(
                        "forbidden",
                        "You do not have sufficient permissions to access this resource. " +
                                "Missing these permissions: ${missingPermissions.joinToString(", ")}"
                )
        )
)
