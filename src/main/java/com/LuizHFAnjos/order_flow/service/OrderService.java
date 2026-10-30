package com.LuizHFAnjos.order_flow.service;

import com.LuizHFAnjos.order_flow.controller.dto.OrderResponse;
import com.LuizHFAnjos.order_flow.dto.OrderCreatedEvent;
import com.LuizHFAnjos.order_flow.entity.OrderEntity;
import com.LuizHFAnjos.order_flow.entity.OrderItem;
import com.LuizHFAnjos.order_flow.repository.OrderRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public void save(OrderCreatedEvent event){
        var entity = new OrderEntity();
        entity.setOrderId(event.codigoPedido());
        entity.setCustomerId(event.codigoCliente());
        entity.setItems(getOrderIems(event));
        entity.setTotal(getTotal(event));

        orderRepository.save(entity);

    }

    public Page<OrderResponse> findAllByCustomerId(Long customerId, PageRequest pageRequest){
        var orders = orderRepository.findAllByCustomerId(customerId, pageRequest);

        return orders.map(OrderResponse::fromEntity);
    }

    private BigDecimal getTotal(OrderCreatedEvent event) {
        return event.itens()
                .stream()
                .map(i -> i.preco().multiply(BigDecimal.valueOf(i.quantidade())))
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    private static @NonNull List<OrderItem> getOrderIems(OrderCreatedEvent event) {
        return event.itens().stream()
                .map(i -> new OrderItem(i.produto(), i.quantidade(), i.preco()))
                .toList();
    }


}
