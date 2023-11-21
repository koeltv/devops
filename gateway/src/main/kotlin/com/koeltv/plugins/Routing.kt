package com.koeltv.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/messages") {
            call.proxyTo("http://monitor:8080")
        }
        route("/state") {
            get {
                call.proxyTo("http://monitor:8080/state")
            }
        }
        get("/run-log") {
            call.proxyTo("http://monitor:8080/run-log")
        }
        get("/mqstatistic") {
            call.proxyTo("http://broker:15672/api/overview") {
                method = HttpMethod.Get
                headers[HttpHeaders.Authorization] = "Basic Z3Vlc3Q6Z3Vlc3Q="
            }
        }
    }
}
