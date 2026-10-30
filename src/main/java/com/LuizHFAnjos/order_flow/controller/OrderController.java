package com.LuizHFAnjos.order_flow.controller;

import com.LuizHFAnjos.order_flow.controller.dto.ApiResponse;
import com.LuizHFAnjos.order_flow.controller.dto.OrderResponse;
import com.LuizHFAnjos.order_flow.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/customers/{customerId}/orders")
    public ResponseEntity<ApiResponse<OrderResponse>> listersOrders(@RequestParam(name = "page", defaultValue = "0") Integer page,
                                                                    @RequestParam(name = "PageSize", defaultValue = "10") Integer pageSize){

        return ResponseEntity.ok(null);
    }
}
