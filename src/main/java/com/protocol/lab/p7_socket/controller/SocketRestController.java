package com.protocol.lab.p7_socket.controller;

import com.protocol.lab.p7_socket.service.SocketSimulatorService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/socket")
@RequiredArgsConstructor
public class SocketRestController {

    private final SocketSimulatorService socketSimulatorService;

    @Data
    public static class TcpStickyRequest {
        private List<String> messages = List.of(
                "ORDER:ORD-9001:AMOUNT=150",
                "ORDER:ORD-9002:AMOUNT=320",
                "ORDER:ORD-9003:AMOUNT=80"
        );
    }

    @Data
    public static class UdpRequest {
        private String message = "PING:GPS_LAT=21.0285:LNG=105.8542";
    }

    @PostMapping("/tcp/sticky-test")
    public ResponseEntity<Map<String, Object>> testTcpStickyPackets(@RequestBody(required = false) TcpStickyRequest req) {
        List<String> messages = (req != null && req.getMessages() != null && !req.getMessages().isEmpty())
                ? req.getMessages()
                : List.of("MSG_A_100", "MSG_B_200", "MSG_C_300");
        return ResponseEntity.ok(socketSimulatorService.simulateTcpStickyPackets(messages));
    }

    @PostMapping("/udp/send")
    public ResponseEntity<Map<String, Object>> sendUdpDatagram(@RequestBody(required = false) UdpRequest req) {
        String msg = (req != null && req.getMessage() != null) ? req.getMessage() : "HEARTBEAT:NODE_1";
        return ResponseEntity.ok(socketSimulatorService.simulateUdpDatagram(msg));
    }

    @GetMapping("/logs")
    public ResponseEntity<List<SocketSimulatorService.SocketLogEvent>> getLogs() {
        return ResponseEntity.ok(socketSimulatorService.getSocketLogs());
    }

    @PostMapping("/logs/clear")
    public ResponseEntity<Map<String, Object>> clearLogs() {
        socketSimulatorService.clearLogs();
        return ResponseEntity.ok(Map.of("success", true));
    }
}
