package com.ssrn.papers.utils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.ssrn.papers.utils.ExceptionUtils.throwAsUncheckedException;

public class CompletableFutureUtils {

    private static final int MILLISECONDS_IN_A_SECOND = 1000;

    public static <T> void rethrowFirstExceptionThrownBy(List<CompletableFuture<T>> completableFutures, int secondsBetweenCheckingCompletedFutures) {
        while (completableFutures.stream().noneMatch(CompletableFuture::isDone)) {
            try {
                Thread.sleep(secondsBetweenCheckingCompletedFutures * MILLISECONDS_IN_A_SECOND);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        completableFutures.stream()
                .filter(CompletableFuture::isCompletedExceptionally)
                .findFirst()
                .ifPresent(completableFuture -> {
                    try {
                        completableFuture.get();
                    } catch (InterruptedException | ExecutionException e) {
                        throwAsUncheckedException(e.getCause());
                    }
                });
    }
}
