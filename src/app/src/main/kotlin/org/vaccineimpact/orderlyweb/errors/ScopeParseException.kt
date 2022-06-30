package org.vaccineimpact.orderlyweb.errors

class ScopeParseException(raw: String) : Exception(
        "Unable to parse '$raw' as a scope. " +
                "It should be either the global scope '*' " +
                "or a specific scope identifier in the form 'prefix:<urlKey>'"
)
