package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.models.permissions.UserGroup

data class UserGroupViewModel(val name: String, val members: List<ReportReaderViewModel>)
{
    companion object
    {
        fun build(userGroup: UserGroup): UserGroupViewModel
        {
            return UserGroupViewModel(userGroup.name, userGroup.members.map {
                ReportReaderViewModel.build(it)
            }.sortedBy { it.displayName })
        }

    }
}
