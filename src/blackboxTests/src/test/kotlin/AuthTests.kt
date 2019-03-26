package org.vaccineimpact.orderlyweb.blackboxTests

import com.spotify.docker.client.DefaultDockerClient
import com.spotify.docker.client.messages.ContainerConfig
import com.spotify.docker.client.messages.HostConfig
import com.spotify.docker.client.messages.PortBinding
import khttp.structures.authorization.Authorization
import org.assertj.core.api.Assertions
import org.junit.After
import org.junit.Test
import org.vaccineimpact.orderlyweb.test_helpers.MontaguTests
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class AuthTests : MontaguTests()
{
    private val docker = DefaultDockerClient.fromEnv().build()
    private var containerId: String? = null

    @After
    fun cleanUp()
    {
        docker.killContainer(containerId)
        docker.removeContainer(containerId)
        docker.close()
        File("tmp").delete()
    }

    @Test
    fun `gets informative error if GitHub org does not exist`()
    {
        val fakeConfigLine = "app.github_org=hdyeiksn"
        runWithConfig(fakeConfigLine)
        Thread.sleep(3000)
        val token = "db5920039c7d88fd976cbdab1da8e531c1148fcf".reversed()
        val result = khttp.post("http://localhost:8081/api/v1/login", auth = GithubTokenHeader(token))
        Assertions.assertThat(result.statusCode).isEqualTo(500)
        Assertions.assertThat(result.text).contains("GitHub org hdyeiksn does not exist")
    }

    private fun runWithConfig(fakeConfigLine: String)
    {
        val configFile = File("tmp")
        configFile.createNewFile()

        configFile.writeText(fakeConfigLine)

        // run app with config
        val hostConfig = HostConfig.builder()
                .appendBinds("${configFile.absolutePath}:/etc/orderly/web/config.properties")
                .portBindings(mapOf("8081" to listOf(PortBinding.of("0.0.0.0", 8081))))
                .build()

        val containerConfig = ContainerConfig.builder()
                .hostConfig(hostConfig)
                .image("docker.montagu.dide.ic.ac.uk:5000/orderly-web:${getCurrentGitBranch()}")
                .exposedPorts("8081")
                .build()

        docker.pull("docker.montagu.dide.ic.ac.uk:5000/orderly-web:${getCurrentGitBranch()}")

        val creation = docker.createContainer(containerConfig);
        val id = creation.id()

        docker.startContainer(id)
        containerId = id



        docker.execStart(docker.execCreate(id, arrayOf("touch", "/etc/orderly/web/go_signal")).id())
    }

    private fun getCurrentGitBranch(): String
    {
        val process = Runtime.getRuntime().exec("git rev-parse --short=7 HEAD")
        process.waitFor()

        val reader = BufferedReader(
                InputStreamReader(process.inputStream))

        return reader.readLine()
    }

    data class GithubTokenHeader(val token: String, val prefix: String = "token") : Authorization
    {
        override val header: Pair<String, String>
            get()
            {
                return "Authorization" to "$prefix $token"
            }
    }
}