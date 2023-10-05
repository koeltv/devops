package com.koeltv.plugins

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

fun Application.configureQueuing() {
    val rabbitMQInstance = install(RabbitMQ) {
        uri = "amqp://$user:$password@$url:$port"

        enableLogging()

        serialize { (it as String).toByteArray() }
        deserialize { bytes, _ -> String(bytes) }

        initialize {
            exchangeDeclare(EXCHANGE, "direct", true)

            queueDeclare(MESSAGE_QUEUE, true, false, false, emptyMap())
            queueBind(MESSAGE_QUEUE, EXCHANGE, MESSAGE_QUEUE)

            queueDeclare(LOG_QUEUE, true, false, false, emptyMap())
            queueBind(LOG_QUEUE, EXCHANGE, LOG_QUEUE)
        }
    }

    rabbitConsumer {
        consume<String>(MESSAGE_QUEUE) {
            rabbitMQInstance.publish(EXCHANGE, LOG_QUEUE, null, "$it MSG")
        }
    }
}

fun ApplicationCall.publishToLog(message: String) {
    publish(EXCHANGE, LOG_QUEUE, null, message)
}