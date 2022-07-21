package org.vaccineimpact.orderlyweb.errors

import org.eclipse.jetty.http.HttpStatus
import org.vaccineimpact.orderlyweb.models.ErrorInfo

class DuplicateKeyError(fields: Map<String, String>) : OrderlyWebError(HttpStatus.CONFLICT_409, fields.map {
    ErrorInfo(
            "duplicate-key:${it.key}",
            "An object with the id '${it.value}' already exists"
    )
})
