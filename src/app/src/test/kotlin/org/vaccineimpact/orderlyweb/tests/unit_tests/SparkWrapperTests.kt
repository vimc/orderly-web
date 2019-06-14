package org.vaccineimpact.orderlyweb.tests.unit_tests

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.vaccineimpact.orderlyweb.SparkServiceWrapper
import org.vaccineimpact.orderlyweb.errors.UnsupportedValueException
import spark.ResponseTransformer
import spark.Route
import spark.route.HttpMethod

class SparkWrapperTests: TeamcityTests()
{
    private val mockRoute = mock<Route>()
    private val mockTransformer = mock<ResponseTransformer>()

}