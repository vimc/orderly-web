package org.vaccineimpact.orderlyweb.appstart

import java.io.File

// This is so that we can copy files into the Docker container after it exists
// but before the API starts running.
fun waitForGoSignal()
{
    val path = File("/etc/montagu/reports_api/go_signal")
    println("Waiting for signal file at $path.")
    println("(In development environments, run `sudo touch $path`)")

    while (!path.exists())
    {
        Thread.sleep(2000)
    }
    println("Go signal detected. Running API")
}