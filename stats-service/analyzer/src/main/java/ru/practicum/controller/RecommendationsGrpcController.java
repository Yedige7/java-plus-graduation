package ru.practicum.controller;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.proto.*;
import ru.practicum.model.InteractionsCountProjection;
import ru.practicum.model.RecommendedEventProjection;
import ru.practicum.service.RecommendationService;

@GrpcService
@RequiredArgsConstructor
public class RecommendationsGrpcController
        extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final RecommendationService recommendationService;

    @Override
    public void getSimilarEvents(
            SimilarEventsRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver
    ) {
        for (RecommendedEventProjection event : recommendationService.getSimilarEvents(
                request.getEventId(),
                request.getUserId(),
                request.getMaxResults()
        )) {
            responseObserver.onNext(
                    RecommendedEventProto.newBuilder()
                            .setEventId(event.getEventId())
                            .setScore(event.getScore())
                            .build()
            );
        }

        responseObserver.onCompleted();
    }

    @Override
    public void getRecommendationsForUser(
            UserPredictionsRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver
    ) {

        for (RecommendedEventProjection event :
                recommendationService.getRecommendationsForUser(
                        request.getUserId(),
                        request.getMaxResults()
                )) {

            responseObserver.onNext(
                    RecommendedEventProto.newBuilder()
                            .setEventId(event.getEventId())
                            .setScore(event.getScore())
                            .build()
            );
        }

        responseObserver.onCompleted();
    }

    @Override
    public void getInteractionsCount(
            InteractionsCountRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver
    ) {

        for (InteractionsCountProjection event :
                recommendationService.getInteractionsCount(
                        request.getEventIdList()
                )) {

            responseObserver.onNext(
                    RecommendedEventProto.newBuilder()
                            .setEventId(event.getEventId())
                            .setScore(event.getScore())
                            .build()
            );
        }

        responseObserver.onCompleted();
    }
}
