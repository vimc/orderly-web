package org.vaccineimpact.orderlyweb.blackboxTests

import com.spotify.docker.client.DefaultDockerClient
import com.spotify.docker.client.messages.ContainerConfig
import com.spotify.docker.client.messages.HostConfig
import com.spotify.docker.client.messages.PortBinding
import org.junit.After
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

abstract class CustomConfigTests : TeamcityTests()
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

    protected fun runWithConfig(fakeConfig: String)
    {
        val configFile = File("tmp")
        configFile.createNewFile()

        configFile.writeText(fakeConfig)

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
}