package org.vaccineimpact.orderlyweb.errors

import org.vaccineimpact.orderlyweb.models.ErrorInfo

class OrderlyFileNotFoundError(filename: String) : OrderlyWebError(404, listOf(
        ErrorInfo("file-not-found", "File with name '$filename' does not exist. ")
))
