package org.vaccineimpact.orderlyweb.db

interface UserData
{
    fun addUser(username: String, email: String)
}

class OrderlyUserData : UserData
{
    override fun addUser(username: String, email: String)
    {
        JooqContext().use {

            val user = it.dsl.selectFrom(Tables.ORDERLYWEB_USER)
                    .where(Tables.ORDERLYWEB_USER.USERNAME.eq(username))
                    .singleOrNull()

            if (user == null)
            {
                it.dsl.newRecord(Tables.ORDERLYWEB_USER)
                        .apply {
                            this.username = username
                            this.email = email
                        }.store()

                it.dsl.newRecord(Tables.ORDERLYWEB_USER_GROUP)
                        .apply {
                            this.id = username
                        }.store()
            }
        }
    }
}