package org.vaccineimpact.orderlyweb.models.permissions

data class PermissionDTO(val name: String,
                         val scopePrefix: String?,
                         val scopeId: String?)
