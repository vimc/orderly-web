package org.vaccineimpact.orderlyweb.customConfigTests

import io.specto.hoverfly.junit.core.HoverflyConfig.localConfigs
import io.specto.hoverfly.junit.rule.HoverflyRule
import org.junit.After
import org.junit.Before
import org.junit.ClassRule
import org.openqa.selenium.By
import org.openqa.selenium.Proxy
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyUserRepository
import org.vaccineimpact.orderlyweb.models.UserSource
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import java.util.concurrent.TimeUnit


abstract class SeleniumTest : CustomConfigTests()
{
    protected lateinit var driver: WebDriver
    protected lateinit var wait: WebDriverWait

    companion object
    {
        @JvmField
        @ClassRule
        var hoverflyRule = HoverflyRule.inCaptureOrSimulationMode("github-oauth2-login.json",
                localConfigs().captureAllHeaders())
    }

    @Before
    fun setup()
    {
        val proxy = Proxy()
        proxy.noProxy = "localhost"
        proxy.httpProxy = "localhost:" + hoverflyRule.proxyPort
        proxy.sslProxy = "localhost:" + hoverflyRule.proxyPort

        driver = ChromeDriver(org.openqa.selenium.chrome.ChromeOptions()
                .apply {
                    addArguments("--ignore-certificate-errors", "--headless", "--no-sandbox")
                    setProxy(proxy)
                })
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS)
        wait = WebDriverWait(driver, 12)
    }

    @After
    fun tearDown()
    {
        driver.quit()
    }

    protected fun clickOnLandingPageLink()
    {
        //Click on the landing page link to navigate to auth provider
        driver.findElement(By.className("btn-xl")).click()
    }

    protected fun loginWithGithub()
    {
        val loginField = driver.findElement(By.id("login_field"))
        val passwordField = driver.findElement(By.id("password"))

        val pw = "notarealpassword"
        val username = "notarealuser"

        loginField.sendKeys(username)
        passwordField.sendKeys(pw)

        driver.findElement(By.name("commit")).click()
    }

    protected fun loginWithMontagu()
    {
        driver.get(RequestHelper.webBaseUrl)

        //We should not hit the landing page for Montagu login, but be taken straight to Montagu
        driver.findElement(By.name("email")).sendKeys("test.user@example.com")
        driver.findElement(By.name("password")).sendKeys("password")
        driver.findElement(By.id("login-button")).click()

        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("site-title")))
    }

    protected fun logout()
    {
        //logout of Orderly Web
        driver.get("${RequestHelper.webBaseUrl}/logout")

        //Should have automatically logged out from Montagu
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("login-button")))

    }

    protected fun addUserWithPermissions(permissions: List<ReifiedPermission>, email: String = "test.user@example.com")
    {
        val userRepo = OrderlyUserRepository()
        userRepo.addUser(email, email, email, UserSource.CLI)

        val authRepo = OrderlyAuthorizationRepository()

        authRepo.ensureGroupHasMember(email, email)
        for (permission in permissions)
        {
            authRepo.ensureUserGroupHasPermission(email, permission)
        }
    }

    protected fun giveUserPermissions(email: String, vararg permissions: ReifiedPermission)
    {
        val authRepo = OrderlyAuthorizationRepository()

        for (permission in permissions)
        {
            authRepo.ensureUserGroupHasPermission(email, permission)
        }
    }

    protected fun addUserGroupWithPermissions(userGroupId: String, members: List<String>, permissions: List<ReifiedPermission>)
    {
        val authRepo = OrderlyAuthorizationRepository()

        authRepo.createUserGroup(userGroupId)
        members.forEach { authRepo.ensureGroupHasMember(userGroupId, it) }

        permissions.forEach{ authRepo.ensureUserGroupHasPermission(userGroupId, it) }

    }

}
