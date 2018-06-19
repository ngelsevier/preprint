package com.ssrn.papers.utils;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.core.CombinableMatcher.either;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class CompletableFutureUtilsTest {
    @Test(expected = NullPointerException.class)
    public void shouldRethrowExceptionThrownInCompletableFutureRunnable() {
        List<CompletableFuture<Void>> completableFutures = singletonList(CompletableFuture.runAsync(() -> {
            throw new NullPointerException();
        }));

        CompletableFutureUtils.rethrowFirstExceptionThrownBy(completableFutures, 1);
    }

    @Test()
    public void shouldOnlyPropagateFirstEncounteredException() {
        NullPointerException anException = new NullPointerException();
        NullPointerException anotherException = new NullPointerException();

        List<CompletableFuture<Void>> completableFutures = asList(CompletableFuture.runAsync(() -> {
                    throw anException;
                }),
                CompletableFuture.runAsync(() -> {
                    throw anotherException;
                }));

        try {
            CompletableFutureUtils.rethrowFirstExceptionThrownBy(completableFutures, 1);
            fail("Expected exception to be thrown");
        } catch (NullPointerException e) {
            assertThat(e, is(either(sameInstance(anException)).or(sameInstance(anotherException))));
        }
    }

}