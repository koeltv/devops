package com.koeltv.plugins

import com.koeltv.CustomEngineMain
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

private val allowedPayloads = listOf("PAUSED", "RUNNING", "SHUTDOWN")

fun Application.configureRouting() {
    routing {
        get("/messages") {
            call.proxyTo("http://monitor:8080")
        }
        route("/state") {
            get {
                call.proxyTo("http://monitor:8080/state")
            }
            put {
                val state = call.receiveText()

                if (state !in allowedPayloads) {
                    call.respond(HttpStatusCode.BadRequest)
                } else {
                    proxyClient.post("http://broker:15672/api/exchanges/%2f/fanout-state/publish") {
                        basicAuth("guest", "guest")
                        contentType(ContentType.Application.Json)
                        setBody("""{"properties":{},"routing_key":"","payload":"$state","payload_encoding":"string"}""")
                    }

                    call.respond(HttpStatusCode.OK)

                    if (state == "SHUTDOWN") {
                        runCatching { proxyClient.get("http://broker:5000") }
                        CustomEngineMain.shutdown()
                    }
                }
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
