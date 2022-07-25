package org.vaccineimpact.orderlyweb.db


fun permissionIsGlobal() = Tables.ORDERLYWEB_USER_GROUP_PERMISSION_ALL.SCOPE_PREFIX.eq("*")
fun permissionIsScopedToReport(report: String) =
        Tables.ORDERLYWEB_USER_GROUP_PERMISSION_ALL.SCOPE_PREFIX.eq("report")
                .and(Tables.ORDERLYWEB_USER_GROUP_PERMISSION_ALL.SCOPE_ID.eq(report))
