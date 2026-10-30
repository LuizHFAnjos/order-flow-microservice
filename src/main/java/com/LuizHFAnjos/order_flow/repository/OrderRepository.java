package com.LuizHFAnjos.order_flow.repository;

import com.LuizHFAnjos.order_flow.entity.OrderEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<OrderEntity,Long> {
}
