package com.LuizHFAnjos.order_flow.dto;

import java.math.BigDecimal;

public record OrderItemEvent(String produto, Integer quantidade, BigDecimal preco) {
}
