package com.koeltv.monitor;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MonitorApplication {
    static final String EXCHANGE_NAME = "exchange";
    static final String LOG_QUEUE_NAME = "log";
    static final String FANOUT_EXCHANGE_NAME = "fanout-state";
    static final String STATE_QUEUE_NAME = "monitor-state";

    @Bean("log")
    Queue logQueue() {
        return new Queue(LOG_QUEUE_NAME);
    }

    @Bean("state")
    Queue stateQueue() {
        return new Queue(STATE_QUEUE_NAME);
    }

    @Bean("exchange")
    DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean("fanoutStateExchange")
    FanoutExchange fanoutStateExchange() {
        return new FanoutExchange(FANOUT_EXCHANGE_NAME);
    }

    @Bean
    Binding logBinding(@Qualifier("log") Queue queue, @Qualifier("exchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(LOG_QUEUE_NAME);
    }

    @Bean
    Binding stateBinding(@Qualifier("state") Queue queue, @Qualifier("fanoutStateExchange") FanoutExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange);
    }

    @Bean
    SimpleMessageListenerContainer logContainer(ConnectionFactory connectionFactory, LogMessageReceiver receiver) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(LOG_QUEUE_NAME);
        container.setMessageListener(new MessageListenerAdapter(receiver, "receiveMessage"));
        return container;
    }

    @Bean
    SimpleMessageListenerContainer stateContainer(ConnectionFactory connectionFactory, StateMessageReceiver receiver) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(STATE_QUEUE_NAME);
        container.setMessageListener(new MessageListenerAdapter(receiver, "receiveMessage"));
        return container;
    }

    public static void main(String[] args) {
        SpringApplication.run(MonitorApplication.class, args);
    }
}
