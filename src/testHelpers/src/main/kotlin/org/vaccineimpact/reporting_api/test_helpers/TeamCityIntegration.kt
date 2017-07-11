package org.vaccineimpact.reporting_api.test_helpers

class TeamCityIntegration : org.junit.rules.TestWatcher()
{
    override fun starting(description: org.junit.runner.Description)
    {
        println("##teamcity[testStarted name='${escape(description.name())}']")
    }

    override fun finished(description: org.junit.runner.Description)
    {
        println("##teamcity[testFinished name='${escape(description.name())}']")
    }

    override fun failed(e: Throwable, description: org.junit.runner.Description)
    {
        println("##teamcity[testFailed name='${escape(description.name())}' " +
                "message='${escape(e.message)}' " +
                "details='${escape(e.toString())}']")
    }

    private fun org.junit.runner.Description.name() = "${this.className}.${this.methodName}"

    private fun escape(text: String?) = text
            ?.replace("|", "||")
            ?.replace("'", "|'")
            ?.replace("\r", "|r")
            ?.replace("\n", "|n")
            ?.replace("[", "|[")
            ?.replace("]", "|]")
}

