package org.vaccineimpact.orderlyweb.tests.unit_tests.templates.rules

import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.gargoylesoftware.htmlunit.html.HTMLParser
import freemarker.template.Configuration
import freemarker.template.Template
import freemarker.template.TemplateExceptionHandler
import freemarker.template.Version
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.io.File
import java.io.StringWriter
import java.io.Writer
import java.lang.IllegalStateException
import java.net.URL
import java.util.*
import javax.xml.transform.Source
import org.xmlmatchers.transform.XmlConverters.the
import com.gargoylesoftware.htmlunit.StringWebResponse
import com.gargoylesoftware.htmlunit.WebClient


//This is a simplified kotlinised version of this: https://github.com/Todderz/freemarker-unit-test

class FreemarkerTestRule(val templateName: String, val templatePath: String = "templates") : TestRule
{
    companion object
    {
        const val anyUrl = "http://localhost"
        val anyWindow = WebClient().currentWindow
    }

    private var template: Template? = null

    override fun apply(base: Statement, description: Description): Statement
    {
        return object : Statement() {
            override fun evaluate() {

                loadTemplate()
                //execute the test
                base.evaluate()
            }
        }
    }

    private fun getTemplate() : Template
    {
        return template ?: throw IllegalStateException()
    }

    private fun loadTemplate()
    {
        val config = configureTemplateLoader()
        template = config.getTemplate(templateName)
    }

    private fun configureTemplateLoader(): Configuration
    {
        val config = Configuration(Configuration.VERSION_2_3_26)
        config.setDirectoryForTemplateLoading(File(templatePath))
        config.defaultEncoding = "UTF-8"
        config.locale = Locale.UK
        config.templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER

        //Assume we should include standard layout file
        config.addAutoInclude("layouts/layout.ftl")
        config.addAutoInclude("layouts/layoutwide.ftl")

        return config
    }

    private fun webResponseFor(dataModel: Any): StringWebResponse
    {
        return StringWebResponse(stringResponseFor(dataModel), URL(anyUrl))
    }

    fun writerResponseFor(dataModel: Any): Writer
    {
        val writer = StringWriter()
        getTemplate().process(dataModel, writer)
        return writer
    }

    fun stringResponseFor(dataModel: Any): String
    {
        return writerResponseFor(dataModel).toString()
    }

    fun htmlPageResponseFor(dataModel: Any): HtmlPage
    {
        return HTMLParser.parse(webResponseFor(dataModel), anyWindow)
    }

    fun xmlResponseFor(dataModel: Any): Source
    {
        return the(htmlPageResponseFor(dataModel).asXml())
    }
}