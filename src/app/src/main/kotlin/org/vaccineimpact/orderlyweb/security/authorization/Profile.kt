package org.vaccineimpact.orderlyweb.security.authorization

import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.models.permissions.PermissionSet
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission

private const val MISSING_PERMISSIONS = "missingPermissions"
private const val MISMATCHED_URL = "mismatchedURL"

val CommonProfile.missingPermissions: MutableSet<ReifiedPermission>
    get() = this.getAttributeOrDefault(MISSING_PERMISSIONS, default = mutableSetOf())

val CommonProfile.orderlyWebPermissions: PermissionSet
    get()
    {
        val perms = this.permissions
        return PermissionSet(perms)
    }

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
