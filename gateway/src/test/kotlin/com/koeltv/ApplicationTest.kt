package com.koeltv

import com.koeltv.plugins.testApplicationWithExternals
import io.ktor.client.request.*
import io.ktor.http.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun testGetMessages() = testApplicationWithExternals {
        val response = client.get("/messages")
        assert(response.contentType().let { it == null || it == ContentType.Text.Plain})
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun testGetState() = testApplicationWithExternals {
        val response = client.get("/state")
        assert(response.contentType().let { it == null || it == ContentType.Text.Plain})
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun testPutState() = testApplicationWithExternals {
        val response = client.put("/state") { setBody("RUNNING") }
        assert(response.contentType().let { it == null || it == ContentType.Text.Plain})
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun testGetRunLog() = testApplicationWithExternals {
        val response = client.get("/run-log")
        assert(response.contentType().let { it == null || it == ContentType.Text.Plain})
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun testGetStatistics() = testApplicationWithExternals {
        val response = client.get("/mqstatistic")
        assertEquals(ContentType.Application.Json, response.contentType())
        assertEquals(HttpStatusCode.OK, response.status)
    }
}
