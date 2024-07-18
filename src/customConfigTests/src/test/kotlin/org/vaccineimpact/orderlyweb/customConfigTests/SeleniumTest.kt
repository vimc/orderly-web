package org.vaccineimpact.orderlyweb.customConfigTests

import io.specto.hoverfly.junit.core.Hoverfly
import io.specto.hoverfly.junit.core.SimulationSource
import io.specto.hoverfly.junit5.HoverflyExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.openqa.selenium.By
import org.openqa.selenium.Proxy
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeDriverService
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyUserRepository
import org.vaccineimpact.orderlyweb.models.UserSource
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

@ExtendWith(SeleniumDebugHelper::class)
@ExtendWith(HoverflyExtension::class)
abstract class SeleniumTest : CustomConfigTests()
{
    lateinit var driver: WebDriver
    protected lateinit var wait: WebDriverWait

    @BeforeEach
    fun setup(hoverfly: Hoverfly)
    {
        hoverfly.simulate(SimulationSource.defaultPath("github-oauth2-login.json"))
        val proxy = Proxy()
        proxy.noProxy = "localhost"
        proxy.httpProxy = "localhost:" + hoverfly.hoverflyConfig.proxyPort
        proxy.sslProxy = "localhost:" + hoverfly.hoverflyConfig.proxyPort
        System.setProperty("webdriver.chrome.whitelistedIps", "")
        val chromeDriverService = ChromeDriverService.createDefaultService()
        chromeDriverService.sendOutputTo(FileOutputStream("/dev/null"))
        driver = ChromeDriver(chromeDriverService, ChromeOptions()
                .apply {
                    addArguments(
                            "--ignore-certificate-errors", "--no-sandbox",
                            "--disable-dev-shm-usage", "--headless"
                    )
                    setProxy(proxy)
                })

        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS)
        wait = WebDriverWait(driver, 60)
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

        val body = driver.findElement(By.cssSelector("body"));
        val html = body.getAttribute("innerHTML")
        println(html)

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

        permissions.forEach { authRepo.ensureUserGroupHasPermission(userGroupId, it) }

    }

}
