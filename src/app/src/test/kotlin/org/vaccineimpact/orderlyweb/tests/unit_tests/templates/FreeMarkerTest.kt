package org.vaccineimpact.orderlyweb.tests.unit_tests.templates

import com.gargoylesoftware.htmlunit.StringWebResponse
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.WebWindow
import com.gargoylesoftware.htmlunit.html.HTMLParser
import com.gargoylesoftware.htmlunit.html.HtmlPage
import freemarker.template.Configuration
import freemarker.template.Template
import freemarker.template.TemplateExceptionHandler
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.vaccineimpact.orderlyweb.app_start.buildFreemarkerConfig
import org.xmlmatchers.transform.XmlConverters.the
import java.io.File
import java.io.StringWriter
import java.io.Writer
import java.net.URL
import javax.xml.transform.Source

//This is a simplified kotlinised version of this: https://github.com/Todderz/freemarker-unit-test
open class FreeMarkerTest(val templateName: String, val templatePath: String = "templates")
{
    companion object
    {
        const val anyUrl = "http://localhost"

        private fun getWebClient(): WebClient
        {
            val client = WebClient()
            client.isThrowExceptionOnFailingStatusCode = false
            client.isThrowExceptionOnScriptError = false
            return client
        }

        val anyWindow: WebWindow = getWebClient().currentWindow
    }

    private val template: Template

    init {
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
        template.process(dataModel, writer)
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

    fun jsoupDocFor(dataModel: Any): Document
    {
        val stringResponse = stringResponseFor(dataModel)
        return Jsoup.parse(stringResponse)
    }
}