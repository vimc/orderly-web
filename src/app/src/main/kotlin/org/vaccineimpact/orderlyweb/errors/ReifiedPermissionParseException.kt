package org.vaccineimpact.orderlyweb.errors

class ReifiedPermissionParseException(raw: String) : Exception(
        "Unable to parse '$raw' as a ReifiedPermission. " +
                "It should have the form 'scope/name' where scope is either the global scope '*' " +
                "or a specific scope identifier in the form 'prefix:id'"
)
