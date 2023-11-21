package com.koeltv

import com.koeltv.plugins.configureQueuing
import com.koeltv.plugins.configureRouting
import io.ktor.server.application.*

@Suppress("unused")
fun Application.module() {
    print("Waiting 2 seconds... ")
    Thread.sleep(2000)
    println("done")

    if (!developmentMode) configureQueuing()
    configureRouting()
}
