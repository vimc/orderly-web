package org.vaccineimpact.reporting_api.test_helpers

import org.junit.Rule

abstract class MontaguTests {
    @get:Rule
    val teamCityIntegration = TeamCityIntegration()
}