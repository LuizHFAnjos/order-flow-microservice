package com.LuizHFAnjos.order_flow.dto;

import java.util.List;

public record OrderCreatedEvent(Long codigoPedido, Long codigoCliente, List<OrderItemEvent> itens) {
}
