package org.vaccineimpact.reporting_api.errors

import org.vaccineimpact.api.models.ErrorInfo

class OrderlyFileNotFoundError(filename: String) : MontaguError(400, listOf(
        ErrorInfo("Bad request", "File with name $filename does not exist. ")
))
