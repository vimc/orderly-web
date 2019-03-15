package org.vaccineimpact.orderlyweb.db

interface UserData
{
    fun addGithubUser(username: String, email: String)
}

class OrderlyUserData : UserData
{
    override fun addGithubUser(username: String, email: String)
    {
        addUser(username, email, "github")
    }

    private fun addUser(username: String, email: String, source: String)
    {
        JooqContext().use {

            val user = it.dsl.selectFrom(Tables.ORDERLYWEB_USER)
                    .where(Tables.ORDERLYWEB_USER.EMAIL.eq(email))
                    .singleOrNull()

            if (user == null)
            {
                it.dsl.newRecord(Tables.ORDERLYWEB_USER)
                        .apply {
                            this.username = username
                            this.email = email
                            this.userSource = source
                        }.store()

                it.dsl.newRecord(Tables.ORDERLYWEB_USER_GROUP)
                        .apply {
                            this.id = email
                        }.store()
            }
        }
    }
}