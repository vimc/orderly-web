package org.vaccineimpact.orderlyweb.customConfigTests

class ReportPageTests : CustomConfigTests()
{

    fun `can get report page`()
    {
        startApp("auth.provider=montagu")
    }
}