package org.vaccineimpact.orderlyweb.errors

import org.eclipse.jetty.http.HttpStatus

class FailedLoginError : OrderlyWebError(HttpStatus.UNAUTHORIZED_401, listOf())
