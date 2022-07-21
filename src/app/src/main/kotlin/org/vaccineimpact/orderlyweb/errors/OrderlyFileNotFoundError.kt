package org.vaccineimpact.orderlyweb.errors

import org.eclipse.jetty.http.HttpStatus
import org.vaccineimpact.orderlyweb.models.ErrorInfo

class OrderlyFileNotFoundError(filename: String) : OrderlyWebError(HttpStatus.NOT_FOUND_404, listOf(
        ErrorInfo("file-not-found", "File with name '$filename' does not exist. ")
))
