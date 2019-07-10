package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.models.permissions.UserGroup

data class UserGroupViewModel(val name: String, val members: List<ReportReaderViewModel>,
                              val canRemove: Boolean)
{
    companion object
    {
        fun build(userGroup: UserGroup, canRemove: Boolean): UserGroupViewModel
        {
            return UserGroupViewModel(userGroup.name, userGroup.members.map {
                ReportReaderViewModel.build(it, canRemove = false)
            }.sortedBy { it.displayName }, canRemove)
        }

    }
}
