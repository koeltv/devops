package com.koeltv

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun testFunctionality() = testApplication {
        // Given
        val testString = "test"
        // When
        val response = client.post {
            setBody(testString)
        }
        //Then
        assertEquals(HttpStatusCode.OK, response.status)
    }
}
