package org.vaccineimpact.orderlyweb.models.permissions
import org.vaccineimpact.orderlyweb.models.Scope

// A permission is just a name, like 'coverage.read'
// To be usable, it must be reified with a scope, like:
// */coverage.read (for the global scope)
// modelling-group:IC-Garske/coverage.read (for a more specific scope)
data class ReifiedPermission(
        val name: String,
        val scope: Scope
)
{
    override fun toString() = "$scope/$name"

    fun satisfiedBy(permission: ReifiedPermission)
            = name == permission.name && permission.scope.encompasses(scope)

    companion object
    {
        fun parse(raw: String): ReifiedPermission
        {
            try
            {
                val parts = raw.split('/')
                val rawScope = parts[0]
                val name = parts[1]
                return ReifiedPermission(name, Scope.parse(rawScope))
            } catch (e: Exception)
            {
                throw ReifiedPermissionParseException(raw)
            }
        }
    }
}