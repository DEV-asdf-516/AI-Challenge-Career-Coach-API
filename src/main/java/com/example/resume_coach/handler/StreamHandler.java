package com.example.resume_coach.handler;

@FunctionalInterface
public interface StreamHandler {
    void onToken(String token);

    default void onError(Throwable t) {}
    default void onComplete() {}
}