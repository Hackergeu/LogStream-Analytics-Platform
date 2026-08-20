package com.logstream.backend.grpc;

import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@GrpcService
public class LogIngestionServiceImpl extends LogIngestionServiceGrpc.LogIngestionServiceImplBase {

    @Override
    public void ingestLog(LogMessage request, StreamObserver<IngestAck> responseObserver) {
        // For now, just log what we received to confirm the pipeline works.
        // Next step: send this to Lucene for indexing instead of just logging it.
        log.info("Received log: service={}, level={}, message={}",
                request.getService(), request.getLevel(), request.getMessage());

        String logId = request.getLogId().isEmpty()
                ? UUID.randomUUID().toString()
                : request.getLogId();

        IngestAck ack = IngestAck.newBuilder()
                .setSuccess(true)
                .setLogId(logId)
                .build();

        responseObserver.onNext(ack);
        responseObserver.onCompleted();
    }
}