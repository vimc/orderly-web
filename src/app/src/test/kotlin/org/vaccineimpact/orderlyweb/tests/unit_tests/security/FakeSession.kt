package org.vaccineimpact.orderlyweb.tests.unit_tests.security

import java.util.*
import javax.servlet.ServletContext
import javax.servlet.http.HttpSession
import javax.servlet.http.HttpSessionContext

class FakeSession : HttpSession
{
    val attributes = mutableMapOf<Any?, Any?>()
    override fun getLastAccessedTime(): Long
    {
        TODO("Not yet implemented")
    }

    override fun removeValue(p0: String?)
    {
        TODO("Not yet implemented")
    }

    override fun setMaxInactiveInterval(p0: Int)
    {
        TODO("Not yet implemented")
    }

    override fun getSessionContext(): HttpSessionContext
    {
        TODO("Not yet implemented")
    }

    override fun getValueNames(): Array<String>
    {
        TODO("Not yet implemented")
    }

    override fun getId(): String
    {
        TODO("Not yet implemented")
    }

    override fun removeAttribute(p0: String?)
    {
        TODO("Not yet implemented")
    }

    override fun putValue(p0: String?, p1: Any?)
    {
        TODO("Not yet implemented")
    }

    override fun getAttributeNames(): Enumeration<String>
    {
        TODO("Not yet implemented")
    }

    override fun isNew(): Boolean
    {
        TODO("Not yet implemented")
    }

    override fun getServletContext(): ServletContext
    {
        TODO("Not yet implemented")
    }

    override fun invalidate()
    {
        TODO("Not yet implemented")
    }

    override fun getCreationTime(): Long
    {
        TODO("Not yet implemented")
    }

    override fun getAttribute(p0: String?): Any?
    {
        return this.attributes[p0]
    }

    override fun setAttribute(p0: String?, p1: Any?)
    {
        this.attributes.put(p0, p1)
    }

    override fun getValue(p0: String?): Any
    {
        TODO("Not yet implemented")
    }

    override fun getMaxInactiveInterval(): Int
    {
        TODO("Not yet implemented")
    }
}
