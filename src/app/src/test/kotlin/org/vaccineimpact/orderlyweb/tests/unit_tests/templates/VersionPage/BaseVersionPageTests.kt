package org.vaccineimpact.orderlyweb.tests.unit_tests.templates.VersionPage

import org.junit.ClassRule
import org.vaccineimpact.orderlyweb.tests.unit_tests.templates.rules.FreemarkerTestRule

open class BaseVersionPageTests
{
    companion object
    {
        @ClassRule
        @JvmField
        val template = FreemarkerTestRule("report-page.ftl")
    }
}
