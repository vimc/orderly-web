package org.vaccineimpact.reporting_api.security

class TokenValidationException(message: String): Exception("Token validation failed: $message")
{
    constructor(field: String, expected: String, actual: String)
        : this("$field was '$actual' - it should have been '$expected'")
}