package com.koeltv

import com.koeltv.plugins.configureMonitoring
import com.koeltv.plugins.configureRouting
import io.ktor.server.application.*

@Suppress("unused")
fun Application.module() {
    configureMonitoring()
    configureRouting()
}
