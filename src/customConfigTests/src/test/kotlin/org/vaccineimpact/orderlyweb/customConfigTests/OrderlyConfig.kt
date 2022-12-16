package org.vaccineimpact.orderlyweb.customConfigTests

data class RemoteConfig(
        val driver: String?,
        val primary: Boolean?,
        val default_branch_only: Boolean?,
        val args: Any?
)
{
    constructor() : this(null, null, null, null)
}

data class OrderlyConfig(
        val database: Any?,
        val fields: Any?,
        val changelog: Any?,
        val tags: Any?,
        val global_resources: Any?,
        val remote: LinkedHashMap<String, RemoteConfig>?
)
{
    constructor() : this(null, null, null, null, null, null)
}

enum class ConfigType
{
    GIT_ALLOWED,
    DEFAULT_BRANCH_ONLY
}
