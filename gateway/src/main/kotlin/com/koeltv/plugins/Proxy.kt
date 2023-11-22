package com.koeltv.plugins

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.*
import io.ktor.utils.io.*

internal var proxyClient = HttpClient()

// Extension method to process all the served HTML documents
private fun String.stripDomain(): String = replace(Regex("(https?:)?//[\\w.-]+(:\\d+)?/"), "")

suspend fun ApplicationCall.proxyTo(targetUrl: String, builder: HttpRequestBuilder.() -> Unit = { method = HttpMethod.Get }) {
    val targetResponse = proxyClient.request(targetUrl, builder)

    // Get the headers of the client response.
    val proxiedHeaders = targetResponse.headers

    // Propagates location header, removing the wikipedia domain from it
    proxiedHeaders[HttpHeaders.Location]?.let { response.header(HttpHeaders.Location, it.stripDomain()) }

    respond(object : OutgoingContent.WriteChannelContent() {
        override val contentLength: Long? = proxiedHeaders[HttpHeaders.ContentLength]?.toLong()
        override val contentType: ContentType? = proxiedHeaders[HttpHeaders.ContentType]?.let { ContentType.parse(it) }
        override val headers: Headers = Headers.build {
            appendAll(proxiedHeaders.filter { key, _ ->
                !key.equals(HttpHeaders.ContentType, ignoreCase = true)
                        && !key.equals(HttpHeaders.ContentLength, ignoreCase = true)
            })
        }
        override val status: HttpStatusCode = targetResponse.status
        override suspend fun writeTo(channel: ByteWriteChannel) {
            targetResponse.bodyAsChannel().copyAndClose(channel)
        }
    })
}