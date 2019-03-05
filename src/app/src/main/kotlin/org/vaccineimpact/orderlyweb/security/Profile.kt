package org.vaccineimpact.orderlyweb.security

import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.api.models.permissions.PermissionSet
import org.vaccineimpact.api.models.permissions.ReifiedPermission

private const val USER_OBJECT = "userObject"
private const val MISSING_PERMISSIONS = "missingPermissions"
private const val PERMISSIONS = "montaguPermissions"
private const val MISMATCHED_URL = "mismatchedURL"

var CommonProfile.internalUser: InternalUser?
    get()
    {
        val user = this.getAttribute(USER_OBJECT)
        return if (user != null && user is InternalUser)
        {
            user
        }
        else
        {
            null
        }
    }
    set(value) = this.addAttribute(USER_OBJECT, value)

val CommonProfile.missingPermissions: MutableSet<ReifiedPermission>
    get() = this.getAttributeOrDefault(MISSING_PERMISSIONS, default = mutableSetOf())

var CommonProfile.montaguPermissions: PermissionSet
    get() = this.getAttributeOrDefault(PERMISSIONS, PermissionSet())
    set(value) = this.addAttribute(PERMISSIONS, value)

var CommonProfile.mismatchedURL: String?
    get() = this.getAttribute(MISMATCHED_URL) as String?
    set(value) = this.addAttribute(MISMATCHED_URL, value)

private fun <T> CommonProfile.getAttributeOrDefault(key: String, default: T): T
{
    if (this.attributes.containsKey(key))
    {
        @Suppress("UNCHECKED_CAST")
        return this.attributes[key] as T
    }
    else
    {
        this.addAttribute(key, default)
        return default
    }
}