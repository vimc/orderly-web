package org.vaccineimpact.orderlyweb.models

import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.errors.PermissionRequirementParseException

data class PermissionRequirement(val name: String, val scopeRequirement: ScopeRequirement)
{
    fun reify(context: ActionContext) = ReifiedPermission(name, scopeRequirement.reify(context))

    override fun toString() = "$scopeRequirement/$name"

    companion object
    {
        fun parse(raw: String): PermissionRequirement
        {
            try
            {
                val parts = raw.split('/')
                val rawScope = parts[0]
                val name = parts[1]
                return PermissionRequirement(name, ScopeRequirement.parse(rawScope))
            }
            catch (e: Exception)
            {
                throw PermissionRequirementParseException(raw)
            }
        }
    }
}

sealed class ScopeRequirement(val value: String)
{
    class Global : ScopeRequirement("*")
    class Specific(val prefix: String, val scopeIdUrlKey: String) : ScopeRequirement("$prefix:<$scopeIdUrlKey>")

    fun reify(context: ActionContext) = when (this)
    {
        is Global -> Scope.Global()
        is Specific -> Scope.Specific(prefix, context.params(scopeIdUrlKey))
    }

    override fun toString() = value
    override fun equals(other: Any?) = when (other)
    {
        is ScopeRequirement -> other.toString() == toString()
        else -> false
    }

    override fun hashCode() = toString().hashCode()

    companion object
    {
        fun parse(rawScope: String): ScopeRequirement
        {
            if (rawScope == "*")
            {
                return Global()
            }
            else
            {
                val parts = rawScope.split(':')
                val idKey = parts[1]
                if (!idKey.startsWith('<') || !idKey.endsWith('>'))
                {
                    throw Exception("Unable to parse $rawScope as a scope requirement - missing angle brackets from scope ID URL key")
                }
                return Specific(parts[0], idKey.trim('<', '>'))
            }
        }
    }
}