package org.vaccineimpact.orderlyweb.errors

import org.vaccineimpact.orderlyweb.Serializer
import org.vaccineimpact.orderlyweb.models.ErrorInfo

class PorcelainError(url: String, statusCode: Int, instanceName: String, errors: List<ErrorInfo>?) : OrderlyWebError(
        statusCode,
        (errors?: listOf()).plus(
                ErrorInfo(
                        "${Serializer.instance.convertFieldName(instanceName, '-')}-error",
                        "$instanceName request failed for url $url"
                )
        )
)
