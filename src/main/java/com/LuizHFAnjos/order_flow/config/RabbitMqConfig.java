package com.LuizHFAnjos.order_flow.config;

import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String ORDER_CREATED_QUEUE = "order-created";

    @Bean
    public Declarable orderCreatedQueue() {
        return new Queue(ORDER_CREATED_QUEUE, true);
    }

    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}