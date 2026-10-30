package com.LuizHFAnjos.order_flow.listener;

import com.LuizHFAnjos.order_flow.dto.OrderCreatedEvent;
import com.LuizHFAnjos.order_flow.service.OrderService;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;

import static com.LuizHFAnjos.order_flow.config.RabbitMqConfig.ORDER_CREATED_QUEUE;

@Component
public class OrderCreatedListener {

    private final Logger logger = LoggerFactory.getLogger(OrderCreatedListener.class);

    private final OrderService orderService;

    public OrderCreatedListener(OrderService orderService) {
        this.orderService = orderService;
    }



    @RabbitListener(queues = ORDER_CREATED_QUEUE)
    public void listen(Message<OrderCreatedEvent> message){
        logger.info("Message consumed: {}", message);

        orderService.save(message.getPayload());
    }

}
