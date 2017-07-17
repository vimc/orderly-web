package org.vaccineimpact.reporting_api.errors

import org.vaccineimpact.api.models.ErrorInfo

class OrderlyFileNotFoundError(filename: String) : MontaguError(404, listOf(
        ErrorInfo("file-not-found", "File with name '$filename' does not exist. ")
))
