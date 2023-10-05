package com.koeltv

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.util.concurrent.TimeUnit

/**
 * Custom engine main for easy shutdown, uses [NettyApplicationEngine]
 */
object CustomEngineMain {
    @JvmStatic
    fun main(args: Array<String>) {
        val environment = commandLineEnvironment(args)
        val engine = NettyApplicationEngine(environment)
        engine.addShutdownHook {
            engine.stop(0, 1, TimeUnit.SECONDS)
        }
        engine.start(true)
    }
}