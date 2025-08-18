package com.example.resume_coach.handler;

import com.example.resume_coach.component.OllamaLineSubscriber;

import java.util.concurrent.CompletableFuture;

public class OllamaResponseHandler {
    private final CompletableFuture<Void> future;
    private final OllamaLineSubscriber subscriber;

    public OllamaResponseHandler(CompletableFuture<Void> future, OllamaLineSubscriber subscriber) {
        this.future = future;
        this.subscriber = subscriber;
    }

    public void cancel() {
        if (subscriber != null) {
            subscriber.cancel();
        }
        future.cancel(true);
    }
}
