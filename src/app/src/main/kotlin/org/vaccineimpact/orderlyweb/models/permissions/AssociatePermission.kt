package org.vaccineimpact.orderlyweb.models.permissions

data class AssociatePermission(val name: String,
                               val scopePrefix: String?,
                               val scopeId: String?)