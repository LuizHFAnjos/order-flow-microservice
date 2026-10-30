package com.LuizHFAnjos.order_flow.controller.dto;

import com.LuizHFAnjos.order_flow.entity.OrderEntity;
import org.springframework.core.annotation.Order;

import java.math.BigDecimal;

public record OrderResponse(Long orderId,
                            Long customerId,
                            BigDecimal total) {

    public static OrderResponse fromEntity(OrderEntity entity){
        return new OrderResponse(entity.getOrderId(), entity.getCustomerId(), entity.getTotal());
    }
}
