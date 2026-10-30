package com.LuizHFAnjos.order_flow.controller.dto;

import java.math.BigDecimal;

public record OrderResponse(Long orderId,
                            Long customerId,
                            BigDecimal total) {
}
