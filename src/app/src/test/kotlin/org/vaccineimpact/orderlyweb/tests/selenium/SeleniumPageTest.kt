package org.vaccineimpact.orderlyweb.tests.selenium

import org.junit.After
import org.junit.Before
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

abstract class SeleniumTest<T>(private val entityClass: KClass<T>) where T: SeleniumPage {

    protected lateinit var driver: WebDriver
    protected lateinit var startingPage: T

    @Before
    fun setUp() {

        val options = ChromeOptions()
    //    options.addArguments("--headless")
        driver = ChromeDriver(options)
        startingPage = entityClass.primaryConstructor!!.call(driver)
        startingPage.open()
    }

    @After
    fun tearDown() {
        driver.quit()
    }
}