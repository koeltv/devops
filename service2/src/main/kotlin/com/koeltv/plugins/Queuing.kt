package com.koeltv.plugins

import com.koeltv.CustomEngineMain
import io.ktor.server.application.*
import pl.jutupe.ktor_rabbitmq.RabbitMQ
import pl.jutupe.ktor_rabbitmq.consume
import pl.jutupe.ktor_rabbitmq.publish
import pl.jutupe.ktor_rabbitmq.rabbitConsumer

private val user = System.getenv("RABBIT_MQ_USER") ?: "guest"
private val password = System.getenv("RABBIT_MQ_PASSWORD") ?: "guest"
private val url = System.getenv("RABBIT_MQ_HOST") ?: "localhost"
private val port = System.getenv("RABBIT_MQ_PORT") ?: "5672"

private const val EXCHANGE = "exchange"
private const val MESSAGE_QUEUE = "message"
private const val LOG_QUEUE = "log"
private const val STATE_EXCHANGE = "fanout-state"
private const val STATE_QUEUE = "service2-state"

fun Application.configureQueuing() {
    val rabbitMQInstance = install(RabbitMQ) {
        uri = "amqp://$user:$password@$url:$port"

        enableLogging()

        serialize { (it as String).toByteArray() }
        deserialize { bytes, _ -> String(bytes) }

        initialize {
            exchangeDeclare(EXCHANGE, "direct", true)
            exchangeDeclare(STATE_EXCHANGE, "fanout", true)

            queueDeclare(MESSAGE_QUEUE, true, false, false, emptyMap())
            queueBind(MESSAGE_QUEUE, EXCHANGE, MESSAGE_QUEUE)

            queueDeclare(LOG_QUEUE, true, false, false, emptyMap())
            queueBind(LOG_QUEUE, EXCHANGE, LOG_QUEUE)

            queueDeclare(STATE_QUEUE, true, false, false, emptyMap())
            queueBind(STATE_QUEUE, STATE_EXCHANGE, "")
        }
    }

    rabbitConsumer {
        consume<String>(MESSAGE_QUEUE) {
            rabbitMQInstance.publish(EXCHANGE, LOG_QUEUE, null, "$it MSG")
        }
        consume<String>(STATE_QUEUE) {
            if (it == "SHUTDOWN") CustomEngineMain.shutdown()
        }
    }
}

fun ApplicationCall.publishToLog(message: String) {
    application.attributes.getOrNull(RabbitMQ.RabbitMQKey)
        ?.publish(EXCHANGE, LOG_QUEUE, null, message)
        ?: application.environment.log.info(message)
}