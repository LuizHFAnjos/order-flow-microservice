package com.LuizHFAnjos.order_flow.controller.dto;

public record PaginationResponse(Integer page,
                                 Integer pageSize,
                                 Integer totalElements,
                                 Integer totalPages) {


}
