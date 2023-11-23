package com.koeltv.plugins

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.koeltv.CustomEngineMain
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

private val allowedPayloads = listOf("PAUSED", "RUNNING", "SHUTDOWN")
private val jsonMapper = jacksonObjectMapper()

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
            val overallStats = proxyClient.get("http://broker:15672/api/overview") { basicAuth("guest", "guest") }
                .bodyAsText()
                .let { jsonMapper.readTree(it) }
                .let {
                    mapOf(
                        "total_connections" to it["object_totals"]["connections"].toString(),
                        "total_consumers" to it["object_totals"]["consumers"].toString(),
                        "total_unread_messages" to it["queue_totals"]["messages"].toString(),
                        "recently_published_messages" to it["message_stats"]["publish"].toString(),
                        "message_publish_rate" to it["message_stats"]["publish_details"]["rate"].toString(),
                    )
                }
            val queueStats = proxyClient.get("http://broker:15672/api/queues") { basicAuth("guest", "guest") }
                .bodyAsText()
                .let { jsonMapper.readTree(it) }
                .associate { node ->
                    // Queue that are in fanout exchanges don't have "message_stats" attribute
                    val messageStats: JsonNode? = node["message_stats"]
                    node["name"].toString() to mapOf(
                        "message_delivery_rate" to (messageStats?.let { it["deliver_get_details"]["rate"].toString() } ?: "-1"),
                        "message_publishing_rate" to (messageStats?.let { it["publish_details"]["rate"].toString() } ?: "-1"),
                        "message_delivered_recently" to (messageStats?.let { it["deliver_get"].toString() } ?: "-1"),
                        "message_published_recently" to (messageStats?.let { it["publish"].toString() } ?: "-1"),
                    )
                }

            val jsonMap = jsonMapper.writeValueAsString(overallStats + queueStats)

            call.respondText(jsonMap, ContentType.Application.Json)
        }
    }
}
