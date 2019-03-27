package org.vaccineimpact.orderlyweb.blackboxTests

import com.spotify.docker.client.DefaultDockerClient
import com.spotify.docker.client.DockerClient.LogsParam
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
        try
        {
            docker.killContainer(containerId)
            docker.removeContainer(containerId)
        }
        catch (e: Exception)
        {
            docker.logs(containerId, LogsParam.stdout(), LogsParam.stderr())
                    .use { stream -> println(stream.readFully()) }
        }
        docker.close()
    }

    protected fun runWithConfig(fakeConfig: String)
    {
        // run app with config
        val hostConfig = HostConfig.builder()
                .portBindings(mapOf("8081" to listOf(PortBinding.of("0.0.0.0", 8081))))
                .build()

        // note this will fail if the image does not exist locally
        val containerConfig = ContainerConfig.builder()
                .hostConfig(hostConfig)
                .image("docker.montagu.dide.ic.ac.uk:5000/orderly-web:${getCurrentGitBranch()}")
                .exposedPorts("8081")
                .build()

        val creation = docker.createContainer(containerConfig);
        val id = creation.id()

        docker.startContainer(id)
        containerId = id

        docker.execStart(docker.execCreate(id, arrayOf("mkdir", "/etc/orderly/web/")).id())
        docker.execStart(docker.execCreate(id, arrayOf("echo", fakeConfig, ">>", "/etc/orderly/web/config.properties")).id())
        docker.execStart(docker.execCreate(id, arrayOf("touch", "/etc/orderly/web/go_signal")).id())
    }

    private fun getCurrentGitBranch(): String
    {
        val process = Runtime.getRuntime().exec("git rev-parse --short=7 HEAD")
        process.waitFor()

        val reader = BufferedReader(
                InputStreamReader(process.inputStream))

        val localGitId = reader.readLine()
        // the local git id will not exist inside a docker container, so in that case look
        // for the environment variable $GIT_ID
        return localGitId ?: System.getenv("GIT_ID")
    }
}