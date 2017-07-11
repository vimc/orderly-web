package org.vaccineimpact.reporting_api.security

import org.apache.commons.codec.digest.DigestUtils
import org.pac4j.core.credentials.password.PasswordEncoder
import org.pac4j.core.util.CommonHelper

class BasicSaltedSha512PasswordEncoder(val salt: String) : PasswordEncoder
{
    override fun encode(password: String): String
    {
        CommonHelper.assertNotBlank("salt", salt)
        return DigestUtils.sha512Hex(password + salt)
    }

    override fun matches(plainPassword: String, encodedPassword: String): Boolean
    {
        CommonHelper.assertNotBlank("salt", salt)
        return CommonHelper.areEquals(encode(plainPassword), encodedPassword)
    }
}