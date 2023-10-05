package com.koeltv.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        post {
            val received = call.receiveText()

            val remoteAddress = call.request.origin.let { "${it.remoteAddress}:${it.remotePort}" }

            call.publishToLog("$received $remoteAddress")
            call.respond(HttpStatusCode.OK)
        }
    }
}
