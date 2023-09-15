package com.koeltv

import com.koeltv.plugins.configureLogging
import com.koeltv.plugins.configureRouting
import io.ktor.server.application.*

fun main(args: Array<String>) {
    CustomEngineMain.main(args)
}

@Suppress("unused")
fun Application.module() {
    print("Waiting 2 seconds... ")
    Thread.sleep(2000)
    println("done")

    configureLogging()
    configureRouting()
}
