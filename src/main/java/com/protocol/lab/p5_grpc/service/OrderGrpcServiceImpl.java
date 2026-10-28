package com.protocol.lab.p5_grpc.service;

import com.protocol.lab.p5_grpc.generated.*;
import com.protocol.lab.p5_grpc.repository.OrderRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.Optional;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class OrderGrpcServiceImpl extends OrderGrpcServiceGrpc.OrderGrpcServiceImplBase {

    private final OrderRepository orderRepository;

    /**
     * 1. Unary RPC: 1 Request -> 1 Response
     */
    @Override
    public void createOrder(CreateOrderRequest request, StreamObserver<OrderResponse> responseObserver) {
        log.info("[gRPC Unary] createOrder: customer={}, sku={}, qty={}, price={}",
                request.getCustomerId(), request.getItemSku(), request.getQuantity(), request.getPrice());

        if (request.getQuantity() <= 0) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Quantity must be greater than zero")
                    .asRuntimeException());
            return;
        }

        OrderResponse order = orderRepository.save(
                request.getCustomerId(),
                request.getItemSku(),
                request.getQuantity(),
                request.getPrice()
        );

        responseObserver.onNext(order);
        responseObserver.onCompleted();
    }

    @Override
    public void getOrder(OrderIdRequest request, StreamObserver<OrderResponse> responseObserver) {
        log.info("[gRPC Unary] getOrder: id={}", request.getOrderId());
        Optional<OrderResponse> orderOpt = orderRepository.findById(request.getOrderId());

        if (orderOpt.isEmpty()) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Order not found with ID: " + request.getOrderId())
                    .asRuntimeException());
            return;
        }

        responseObserver.onNext(orderOpt.get());
        responseObserver.onCompleted();
    }

    /**
     * 2. Server Streaming RPC: 1 Request -> Stream of Responses
     */
    @Override
    public void streamOrderStatus(OrderIdRequest request, StreamObserver<OrderStatusUpdate> responseObserver) {
        log.info("[gRPC Server Streaming] streamOrderStatus for orderId={}", request.getOrderId());

        String[] stages = {"ORDER_ACCEPTED", "PAYMENT_CONFIRMED", "WAREHOUSE_PACKING", "COURIER_DISPATCHED", "DELIVERED"};
        String[] messages = {
                "Order received by microservice cluster",
                "Payment transaction authorized via internal ledger",
                "Items picked and packaged at fulfillment center",
                "Courier driver assigned and en-route",
                "Package delivered to customer hands"
        };

        for (int i = 0; i < stages.length; i++) {
            OrderStatusUpdate update = OrderStatusUpdate.newBuilder()
                    .setOrderId(request.getOrderId())
                    .setStage(stages[i])
                    .setMessage(messages[i])
                    .setTimestamp(System.currentTimeMillis())
                    .build();

            responseObserver.onNext(update);
        }

        responseObserver.onCompleted();
        log.info("[gRPC Server Streaming] Completed streaming {} status updates", stages.length);
    }

    /**
     * 3. Client Streaming RPC: Stream of Requests -> 1 Aggregated Response
     */
    @Override
    public StreamObserver<TelemetryPoint> uploadOrderTelemetry(StreamObserver<TelemetrySummary> responseObserver) {
        log.info("[gRPC Client Streaming] Client initiated uploadOrderTelemetry stream");

        return new StreamObserver<TelemetryPoint>() {
            int pointCount = 0;
            double speedSum = 0.0;
            double maxSpeed = 0.0;
            String currentOrderId = "";

            @Override
            public void onNext(TelemetryPoint point) {
                pointCount++;
                currentOrderId = point.getOrderId();
                speedSum += point.getSpeedKmh();
                if (point.getSpeedKmh() > maxSpeed) {
                    maxSpeed = point.getSpeedKmh();
                }
                log.debug("[gRPC Client Streaming] Received telemetry point #{}: lat={}, lng={}, speed={}",
                        pointCount, point.getLatitude(), point.getLongitude(), point.getSpeedKmh());
            }

            @Override
            public void onError(Throwable t) {
                log.error("[gRPC Client Streaming] Stream error from client", t);
            }

            @Override
            public void onCompleted() {
                double avgSpeed = pointCount > 0 ? (speedSum / pointCount) : 0.0;
                log.info("[gRPC Client Streaming] Completed. Total points: {}, Avg Speed: {} km/h", pointCount, avgSpeed);

                TelemetrySummary summary = TelemetrySummary.newBuilder()
                        .setOrderId(currentOrderId)
                        .setTotalPointsReceived(pointCount)
                        .setAverageSpeedKmh(Math.round(avgSpeed * 100.0) / 100.0)
                        .setMaxSpeedKmh(Math.round(maxSpeed * 100.0) / 100.0)
                        .setStatus("TELEMETRY_LOGGED_SUCCESSFULLY")
                        .build();

                responseObserver.onNext(summary);
                responseObserver.onCompleted();
            }
        };
    }

    /**
     * 4. Bidirectional Streaming RPC: Full-duplex real-time events
     */
    @Override
    public StreamObserver<DeliveryEvent> liveDeliveryChannel(StreamObserver<DeliveryEvent> responseObserver) {
        log.info("[gRPC Bi-directional Streaming] Opened live delivery channel");

        return new StreamObserver<DeliveryEvent>() {
            @Override
            public void onNext(DeliveryEvent event) {
                log.info("[gRPC Bi-di] Received event from [{}]: {}", event.getSender(), event.getPayload());

                DeliveryEvent serverReply = DeliveryEvent.newBuilder()
                        .setOrderId(event.getOrderId())
                        .setSender("ORDER_DISPATCH_SYSTEM")
                        .setPayload("ACK: Received event [" + event.getPayload() + "] at " + System.currentTimeMillis())
                        .setTimestamp(System.currentTimeMillis())
                        .build();

                responseObserver.onNext(serverReply);
            }

            @Override
            public void onError(Throwable t) {
                log.warn("[gRPC Bi-di] Live channel error: {}", t.getMessage());
            }

            @Override
            public void onCompleted() {
                log.info("[gRPC Bi-di] Channel closed by client. Sending completion to client.");
                responseObserver.onCompleted();
            }
        };
    }
}
