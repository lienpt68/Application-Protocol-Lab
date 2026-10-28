package com.protocol.lab.p5_grpc;

import com.protocol.lab.p5_grpc.generated.*;
import com.protocol.lab.p5_grpc.repository.OrderRepository;
import com.protocol.lab.p5_grpc.service.OrderGrpcServiceImpl;
import com.protocol.lab.p5_grpc.interceptor.LogAndTraceServerInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.ServerInterceptors;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class OrderGrpcServiceTest {

    private Server inProcessServer;
    private ManagedChannel inProcessChannel;
    private OrderGrpcServiceGrpc.OrderGrpcServiceBlockingStub blockingStub;
    private OrderGrpcServiceGrpc.OrderGrpcServiceStub asyncStub;

    @BeforeEach
    void setUp() throws IOException {
        String serverName = InProcessServerBuilder.generateName();
        OrderRepository orderRepository = new OrderRepository();
        OrderGrpcServiceImpl service = new OrderGrpcServiceImpl(orderRepository);

        LogAndTraceServerInterceptor interceptor = new LogAndTraceServerInterceptor();

        inProcessServer = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(ServerInterceptors.intercept(service, interceptor))
                .build()
                .start();

        inProcessChannel = InProcessChannelBuilder.forName(serverName)
                .directExecutor()
                .build();

        blockingStub = OrderGrpcServiceGrpc.newBlockingStub(inProcessChannel);
        asyncStub = OrderGrpcServiceGrpc.newStub(inProcessChannel);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        if (inProcessChannel != null) {
            inProcessChannel.shutdownNow().awaitTermination(3, TimeUnit.SECONDS);
        }
        if (inProcessServer != null) {
            inProcessServer.shutdownNow().awaitTermination(3, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("1. Unary RPC: Should create and retrieve order successfully")
    void testUnaryCreateAndGetOrder() {
        CreateOrderRequest request = CreateOrderRequest.newBuilder()
                .setCustomerId("CUST-99")
                .setItemSku("SERVER-RACK-42U")
                .setQuantity(2)
                .setPrice(1299.50)
                .build();

        OrderResponse response = blockingStub.createOrder(request);

        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).startsWith("ORD-");
        assertThat(response.getCustomerId()).isEqualTo("CUST-99");
        assertThat(response.getTotalAmount()).isEqualTo(2599.0);
        assertThat(response.getStatus()).isEqualTo("CONFIRMED");

        // Test Get Order
        OrderResponse getResponse = blockingStub.getOrder(
                OrderIdRequest.newBuilder().setOrderId(response.getOrderId()).build()
        );
        assertThat(getResponse.getOrderId()).isEqualTo(response.getOrderId());
    }

    @Test
    @DisplayName("2. Server Streaming RPC: Should stream all lifecycle stages for an order")
    void testServerStreamingOrderStatus() {
        OrderIdRequest request = OrderIdRequest.newBuilder().setOrderId("ORD-1001").build();

        Iterator<OrderStatusUpdate> updates = blockingStub.streamOrderStatus(request);
        List<OrderStatusUpdate> receivedUpdates = new ArrayList<>();
        updates.forEachRemaining(receivedUpdates::add);

        assertThat(receivedUpdates).hasSize(5);
        assertThat(receivedUpdates.get(0).getStage()).isEqualTo("ORDER_ACCEPTED");
        assertThat(receivedUpdates.get(4).getStage()).isEqualTo("DELIVERED");
    }

    @Test
    @DisplayName("3. Client Streaming RPC: Should aggregate telemetry points into single summary")
    void testClientStreamingTelemetry() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final TelemetrySummary[] result = new TelemetrySummary[1];

        StreamObserver<TelemetrySummary> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(TelemetrySummary summary) {
                result[0] = summary;
            }

            @Override
            public void onError(Throwable t) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        };

        StreamObserver<TelemetryPoint> requestObserver = asyncStub.uploadOrderTelemetry(responseObserver);

        // Client streams 3 GPS telemetry points
        requestObserver.onNext(TelemetryPoint.newBuilder().setOrderId("ORD-1001").setSpeedKmh(40.0).setLatitude(21.0285).setLongitude(105.8542).build());
        requestObserver.onNext(TelemetryPoint.newBuilder().setOrderId("ORD-1001").setSpeedKmh(60.0).setLatitude(21.0300).setLongitude(105.8550).build());
        requestObserver.onNext(TelemetryPoint.newBuilder().setOrderId("ORD-1001").setSpeedKmh(50.0).setLatitude(21.0320).setLongitude(105.8560).build());
        requestObserver.onCompleted();

        boolean completedInTime = latch.await(5, TimeUnit.SECONDS);
        assertThat(completedInTime).isTrue();
        assertThat(result[0]).isNotNull();
        assertThat(result[0].getTotalPointsReceived()).isEqualTo(3);
        assertThat(result[0].getAverageSpeedKmh()).isEqualTo(50.0);
        assertThat(result[0].getMaxSpeedKmh()).isEqualTo(60.0);
        assertThat(result[0].getStatus()).isEqualTo("TELEMETRY_LOGGED_SUCCESSFULLY");
    }

    @Test
    @DisplayName("4. Bidirectional Streaming RPC: Should exchange real-time delivery events full-duplex")
    void testBidirectionalStreamingDeliveryChannel() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        List<DeliveryEvent> receivedReplies = new ArrayList<>();

        StreamObserver<DeliveryEvent> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(DeliveryEvent event) {
                receivedReplies.add(event);
                latch.countDown();
            }

            @Override
            public void onError(Throwable t) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
            }
        };

        StreamObserver<DeliveryEvent> requestObserver = asyncStub.liveDeliveryChannel(responseObserver);

        // Gửi tin 1
        requestObserver.onNext(DeliveryEvent.newBuilder()
                .setOrderId("ORD-1001")
                .setSender("COURIER_DEVICE_01")
                .setPayload("Courier picked up package")
                .build());

        // Gửi tin 2
        requestObserver.onNext(DeliveryEvent.newBuilder()
                .setOrderId("ORD-1001")
                .setSender("COURIER_DEVICE_01")
                .setPayload("Courier arrived at customer address")
                .build());

        requestObserver.onCompleted();

        boolean completedInTime = latch.await(5, TimeUnit.SECONDS);
        assertThat(completedInTime).isTrue();
        assertThat(receivedReplies).hasSize(2);
        assertThat(receivedReplies.get(0).getSender()).isEqualTo("ORDER_DISPATCH_SYSTEM");
        assertThat(receivedReplies.get(0).getPayload()).contains("Courier picked up package");
    }

    @Test
    @DisplayName("5. The Backend Engineer Way: Heavy RPC hủy ngang khi vượt quá gRPC Deadline")
    void testProcessHeavyOrderDeadlineExceeded() {
        // Thiết lập deadline siêu ngắn 100ms trong khi xử lý cần ~750ms
        try {
            blockingStub.withDeadlineAfter(100, TimeUnit.MILLISECONDS)
                    .processHeavyOrder(OrderIdRequest.newBuilder().setOrderId("ORD-DEADLINE-01").build());
        } catch (StatusRuntimeException e) {
            assertThat(e.getStatus().getCode()).isEqualTo(Status.Code.DEADLINE_EXCEEDED);
        }
    }
}
