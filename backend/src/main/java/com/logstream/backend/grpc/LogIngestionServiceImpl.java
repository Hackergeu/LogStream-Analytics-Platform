package com.logstream.backend.grpc;

import com.logstream.backend.search.LogIndexService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.service.GrpcService;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class LogIngestionServiceImpl extends LogIngestionServiceGrpc.LogIngestionServiceImplBase {

    private final LogIndexService logIndexService;

    @Override
    public void ingestLog(LogMessage request, StreamObserver<IngestAck> responseObserver) {
        String logId = request.getLogId().isEmpty()
                ? UUID.randomUUID().toString()
                : request.getLogId();

        LogMessage finalMessage = request.toBuilder().setLogId(logId).build();

        boolean success = true;
        try {
            logIndexService.indexLog(finalMessage);
        } catch (IOException e) {
            log.error("Failed to index log_id={}", logId, e);
            success = false;
        }

        IngestAck ack = IngestAck.newBuilder()
                .setSuccess(success)
                .setLogId(logId)
                .build();

        responseObserver.onNext(ack);
        responseObserver.onCompleted();
    }
}
