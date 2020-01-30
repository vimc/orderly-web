package org.vaccineimpact.orderlyweb.errors

import org.vaccineimpact.orderlyweb.models.ErrorInfo

class UsersManageError(message: String): OrderlyWebError(400, listOf(
        ErrorInfo("users-manage-error", message)
))