package org.vaccineimpact.orderlyweb.models.permissions

data class AssociatePermission(val action: String,
                         val name: String,
                         val scopePrefix: String?,
                         val scopeId: String?)