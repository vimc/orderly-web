package org.vaccineimpact.orderlyweb.tests.selenium

import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.PageFactory
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory

abstract class SeleniumPage(protected val driver: WebDriver)
{
    init
    {
        PageFactory.initElements(AjaxElementLocatorFactory(driver, 10), this)
    }

    abstract fun open()
}