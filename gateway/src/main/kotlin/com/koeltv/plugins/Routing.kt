package com.koeltv.plugins

import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/messages") {
            call.proxyTo("http://monitor:8080")
        }
    }
}
