package com.protocol.lab.p5_grpc.exception;

import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

@Slf4j
@GrpcAdvice
public class GrpcExceptionAdvice {

    @GrpcExceptionHandler(OrderNotFoundException.class)
    public Status handleOrderNotFound(OrderNotFoundException e) {
        log.warn("[gRPC Exception Handler] Order not found: {}", e.getMessage());
        return Status.NOT_FOUND.withDescription(e.getMessage()).withCause(e);
    }

    @GrpcExceptionHandler(IllegalArgumentException.class)
    public Status handleIllegalArgument(IllegalArgumentException e) {
        log.warn("[gRPC Exception Handler] Invalid argument: {}", e.getMessage());
        return Status.INVALID_ARGUMENT.withDescription(e.getMessage()).withCause(e);
    }

    @GrpcExceptionHandler(Exception.class)
    public Status handleGenericException(Exception e) {
        log.error("[gRPC Exception Handler] Internal error", e);
        return Status.INTERNAL.withDescription("Internal Server Error: " + e.getMessage());
    }
}
