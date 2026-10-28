package com.protocol.lab.p5_grpc.interceptor;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;

import java.util.UUID;

@Slf4j
@GrpcGlobalServerInterceptor
public class LogAndTraceServerInterceptor implements ServerInterceptor {

    public static final Metadata.Key<String> TRACE_ID_HEADER =
            Metadata.Key.of("x-trace-id", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String incomingTraceId = headers.get(TRACE_ID_HEADER);
        String traceId = (incomingTraceId != null && !incomingTraceId.isBlank())
                ? incomingTraceId
                : UUID.randomUUID().toString().substring(0, 8);

        long start = System.currentTimeMillis();
        String methodName = call.getMethodDescriptor().getFullMethodName();

        ServerCall<ReqT, RespT> wrappedCall = new ForwardingServerCall.SimpleForwardingServerCall<>(call) {
            @Override
            public void sendHeaders(Metadata responseHeaders) {
                responseHeaders.put(TRACE_ID_HEADER, traceId);
                super.sendHeaders(responseHeaders);
            }

            @Override
            public void close(Status status, Metadata trailers) {
                long duration = System.currentTimeMillis() - start;
                log.info("[gRPC Interceptor] [traceId={}] RPC: {} | Status: {} | Duration: {}ms",
                        traceId, methodName, status.getCode(), duration);
                super.close(status, trailers);
            }
        };

        return next.startCall(wrappedCall, headers);
    }
}
