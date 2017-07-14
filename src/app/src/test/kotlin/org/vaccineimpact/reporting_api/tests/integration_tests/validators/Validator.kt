package org.vaccineimpact.reporting_api.tests.integration_tests.validators

interface Validator
{
    fun validateError(response: String,
                      expectedErrorCode: String? = null,
                      expectedErrorText: String? = null,
                      assertionText: String? = null)

    fun validateSuccess(response: String)
}