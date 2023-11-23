package com.koeltv.plugins

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.server.testing.*

private const val DEFAULT_OVERVIEW_RESPONSE = """
{
  "object_totals": {
    "connections": 0,
    "consumers": 0
  },
  "queue_totals": {
    "messages": "0"
  },
  "message_stats": {
    "publish": 0,
    "publish_details": {
      "rate": 0
    }
  }
}
"""

private const val DEFAULT_QUEUES_RESPONSE = "[]"

fun testApplicationWithExternals(block: suspend ApplicationTestBuilder.() -> Unit) {
    proxyClient = HttpClient(MockEngine { request ->
        val url = request.url.toString()
        val method = request.method

        if ("http://broker:15672" in url) {
            if (method == HttpMethod.Get) {
                if ("api/overview" in url) {
                    respond(DEFAULT_OVERVIEW_RESPONSE, headers = headersOf(HttpHeaders.ContentType, "application/json"))
                } else if ("api/queues" in url) {
                    respond(DEFAULT_QUEUES_RESPONSE, headers = headersOf(HttpHeaders.ContentType, "application/json"))
                } else error("Requested unknown route: $method / $url")
            } else if (method == HttpMethod.Post && "api/exchanges/%2f/fanout-state/publish" in url) {
                respondOk()
            } else error("Requested unknown route: $method / $url")
        } else if ("http://broker:5000" in url && method == HttpMethod.Get) {
            respondOk()
        } else if ("http://monitor:8080" in url && method == HttpMethod.Get) {
            respondOk()
        } else error("Requested unknown route: $method / $url")
    })

    testApplication { block() }
}