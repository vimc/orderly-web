package org.vaccineimpact.orderlyweb.test_helpers

import org.junit.Rule

abstract class TeamcityTests
{
    @get:Rule
    val teamCityIntegration = TeamCityIntegration()
}