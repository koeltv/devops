package com.koeltv

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlin.system.exitProcess

/**
 * Custom engine main for easy shutdown, uses [NettyApplicationEngine]
 */
object CustomEngineMain {
    private val semaphore = Semaphore(1, 1)

    @JvmStatic
    fun main(args: Array<String>) {
        val environment = commandLineEnvironment(args)
        NettyApplicationEngine(environment).apply {
            addShutdownHook { stop(0, 1000) }
            start(false)
        }
        runBlocking {
            semaphore.acquire()
            exitProcess(0)
        }
    }

    fun shutdown(): Unit = semaphore.release()
}
