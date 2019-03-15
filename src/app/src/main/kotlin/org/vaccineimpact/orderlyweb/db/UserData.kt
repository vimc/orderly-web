package org.vaccineimpact.orderlyweb.db

interface UserData {

    fun addUser(username: String, email: String)
}

class OrderlyUserData: UserData {

    override fun addUser(username: String, email: String)
    {
        JooqContext().use {
          
        }
    }
}