package org.vaccineimpact.orderlyweb.models.permissions

import org.vaccineimpact.orderlyweb.models.User

data class UserGroup(val name: String, val members: List<User>)