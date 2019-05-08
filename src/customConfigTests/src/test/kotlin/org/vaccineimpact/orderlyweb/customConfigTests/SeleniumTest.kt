package org.vaccineimpact.orderlyweb.customConfigTests

import org.assertj.core.api.Assertions
import org.junit.After
import org.junit.Before
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.vaccineimpact.orderlyweb.db.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.db.OrderlyUserRepository
import org.vaccineimpact.orderlyweb.db.UserRepository
import org.vaccineimpact.orderlyweb.models.UserSource
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission

abstract class SeleniumTest : CustomConfigTests()
{
    protected lateinit var driver: WebDriver

    protected lateinit var wait: WebDriverWait

    @Before
    fun setup()
    {
        driver = ChromeDriver(org.openqa.selenium.chrome.ChromeOptions()
                .apply { addArguments("--ignore-certificate-errors", "--headless", "--no-sandbox")  })
        wait = WebDriverWait(driver, 10)
    }

    @After
    fun tearDown()
    {
        driver.quit()
    }

    protected fun clickOnLandingPageLink()
    {
        //Click on the landing page link to navigate to auth provider
        driver.findElement(By.className("login-external-link")).click()
    }

    protected fun loginWithMontagu()
    {
        driver.get(RequestHelper.webBaseUrl)

        clickOnLandingPageLink()

        driver.findElement(By.name("email")).sendKeys("test.user@example.com")
        driver.findElement(By.name("password")).sendKeys("password")
        driver.findElement(By.id("login-button")).click()

        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("site-title")))
    }

    protected fun logout()
    {
        //logout of Orderly Web
        driver.get("${RequestHelper.webBaseUrl}/logout")

        //..which will take us to out login page - click through to Montagu
        clickOnLandingPageLink()

        //Should have automatically logged out from Montagu
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("login-button")))

    }

    protected fun addUserWithPermissions(permissions: List<ReifiedPermission>, email: String = "test.user@example.com")
    {
        val userRepo = OrderlyUserRepository()
        userRepo.addUser(email, email, email, UserSource.CLI)

        val authRepo = OrderlyAuthorizationRepository()

        authRepo.ensureGroupHasMember(email, email)
        for(permission in permissions) {
            authRepo.ensureUserGroupHasPermission(email, permission)
        }
    }

}
