package com.project.order.service;

import com.project.order.client.InventoryClient;
import com.project.order.dto.OrderRequest;
import com.project.order.event.OrderPlacedEvent;
import com.project.order.model.Order;
import com.project.order.repository.OrderRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;
    public void placeOrder(OrderRequest orderRequest){

        boolean inventoryStatus = inventoryClient.isInStock(orderRequest.skuCode(), orderRequest.quantity());
        log.info("Inventory Status", inventoryStatus);
        if(inventoryStatus){

            Order order = new Order();
            order.setOrderNumber(UUID.randomUUID().toString());
            order.setPrice(orderRequest.price());
            order.setSkuCode(orderRequest.skuCode());
            order.setQuantity(orderRequest.quantity());
            orderRepository.save(order);

            //Send Kafka notification
            OrderPlacedEvent orderPlacedEvent = new OrderPlacedEvent(order.getOrderNumber(), orderRequest.email());
            log.info("Start - sending orderPlacedEvent {} to kafka topic order-placed", orderPlacedEvent);
            kafkaTemplate.send("order-placed", orderPlacedEvent);
            log.info("End - sending orderPlacedEvent {} to kafka topic order-placed", orderPlacedEvent);
        }else {
            throw new RuntimeException("Product with skuCode "+orderRequest.skuCode()+ "is not in stock");
        }
    }
}
