package com.protocol.lab.p5_grpc.repository;

import com.protocol.lab.p5_grpc.generated.OrderResponse;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class OrderRepository {

    private final Map<String, OrderResponse> orderStore = new ConcurrentHashMap<>();
    private final AtomicLong orderSeq = new AtomicLong(1000);

    public OrderResponse save(String customerId, String itemSku, int quantity, double price) {
        String orderId = "ORD-" + orderSeq.incrementAndGet();
        double total = Math.round((quantity * price) * 100.0) / 100.0;
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        OrderResponse order = OrderResponse.newBuilder()
                .setOrderId(orderId)
                .setCustomerId(customerId)
                .setItemSku(itemSku)
                .setQuantity(quantity)
                .setTotalAmount(total)
                .setStatus("CONFIRMED")
                .setCreatedAt(now)
                .build();

        orderStore.put(orderId, order);
        return order;
    }

    public Optional<OrderResponse> findById(String orderId) {
        return Optional.ofNullable(orderStore.get(orderId));
    }
}
