package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.models.permissions.UserGroup

sealed class UserGroupViewModel(val name: String, val members: List<ReportReaderViewModel>)
{
    class IdentityGroupViewModel(name: String) : UserGroupViewModel(name, listOf())
    class MembersGroupViewModel(name: String, members: List<ReportReaderViewModel>) : UserGroupViewModel(name, members)

    companion object
    {
        fun build(userGroup: UserGroup): UserGroupViewModel
        {
            if (userGroup.members.all { it.email == userGroup.name })
            {
                return IdentityGroupViewModel(userGroup.name)
            }

            return MembersGroupViewModel(userGroup.name, userGroup.members.map {
                ReportReaderViewModel.build(it, canRemove = false)
            }.sortedBy { it.displayName })
        }

    }
}
