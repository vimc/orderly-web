package org.vaccineimpact.orderlyweb.app_start

import java.io.File

// This is so that we can copy files into the Docker container after it exists
// but before the API starts running.
const val WAIT_TIME = 500L
fun waitForGoSignal()
{
    val path = File("/etc/orderly/web/go_signal")
    println("Waiting for signal file at $path.")
    println("(In development environments, run `sudo touch $path`)")

    while (!path.exists())
    {
        Thread.sleep(WAIT_TIME)
    }
    println("Go signal detected. Running API")
}
