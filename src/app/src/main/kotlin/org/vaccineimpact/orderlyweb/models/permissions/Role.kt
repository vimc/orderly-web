package org.vaccineimpact.orderlyweb.models.permissions

import org.vaccineimpact.orderlyweb.models.User

data class Role(val name: String, val members: List<User>, val permissions: List<ReifiedPermission>)
