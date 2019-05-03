package org.vaccineimpact.orderlyweb.tests.unit_tests.templates.rules

import com.gargoylesoftware.htmlunit.StringWebResponse
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HTMLParser
import com.gargoylesoftware.htmlunit.html.HtmlPage
import freemarker.template.Configuration
import freemarker.template.Template
import freemarker.template.TemplateExceptionHandler
import khttp.options
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.vaccineimpact.orderlyweb.app_start.buildFreemarkerConfig
import org.xmlmatchers.transform.XmlConverters.the
import java.io.File
import java.io.StringWriter
import java.io.Writer
import java.net.URL
import javax.xml.transform.Source


//This is a simplified kotlinised version of this: https://github.com/Todderz/freemarker-unit-test

class FreemarkerTestRule(val templateName: String, val templatePath: String = "templates") : TestRule
{
    companion object
    {
        const val anyUrl = "http://localhost"
        fun getWebClient(): WebClient
        {
            val client = WebClient()
            client.isThrowExceptionOnScriptError = false
            return client
        }

        val anyWindow = getWebClient().currentWindow
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
        val config = buildFreemarkerConfig(File(templatePath))
        config.templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER

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