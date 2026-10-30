package com.LuizHFAnjos.order_flow.controller.dto;

import java.util.List;

public record ApiResponse<T>(List<T> data, PaginationResponse pagination) {
}
