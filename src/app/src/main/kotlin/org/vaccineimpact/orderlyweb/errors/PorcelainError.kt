package org.vaccineimpact.orderlyweb.errors

import org.vaccineimpact.orderlyweb.Serializer
import org.vaccineimpact.orderlyweb.models.ErrorInfo

class PorcelainError(url: String, statusCode: Int, instanceName: String) : OrderlyWebError(
        statusCode,
        listOf(
                ErrorInfo(
                        "${Serializer.instance.convertFieldName(instanceName)
                                .split(" ").joinToString("-")}-error",
                        "$instanceName request failed for url $url"
                )
        )
)
