package com.koeltv

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import java.util.concurrent.TimeUnit

/**
 * Custom engine main for easy shutdown, uses [NettyApplicationEngine]
 */
object CustomEngineMain {
    private val semaphore = Semaphore(1, 1)

    @JvmStatic
    fun main(args: Array<String>) {
        val environment = commandLineEnvironment(args)
        val engine = NettyApplicationEngine(environment)
        engine.addShutdownHook {
            engine.stop(0, 1, TimeUnit.SECONDS)
        }
        engine.start(false)
        runBlocking { semaphore.acquire() }
    }

    fun shutdown(): Unit = semaphore.release()
}