package org.vaccineimpact.reporting_api.errors

class PermissionRequirementParseException(raw: String)
    : Exception("Unable to parse '$raw' as a PermissionRequirement. " +
        "It should have the form 'scope/name' where scope is either the global scope '*' " +
        "or a specific scope identifier in the form 'prefix:<urlKey>'")