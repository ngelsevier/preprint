package com.ssrn.papers.replicator.concurrency;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.ssrn.test.support.matchers.EventualMatcher.eventuallySatisfies;
import static com.ssrn.test.support.utils.Interval.checkingEvery;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

public class SingleJobRunnerTest {

    @Test
    public void shouldRunProvidedRunnable() {
        // Given
        AtomicBoolean jobRan = new AtomicBoolean(false);
        SingleJobRunner singleJobRunner = new SingleJobRunner();

        // When
        boolean jobAccepted = singleJobRunner.run(() -> jobRan.set(true), throwable -> {
        });

        // Then
        assertThat(jobAccepted, is(equalTo(true)));
        assertThat(jobRan::get, eventuallySatisfies(is(equalTo(true)))
                .within(100, MILLISECONDS, checkingEvery(10, MILLISECONDS)));
    }

    @Test
    public void shouldRunAJobThatIsProvidedAnotherHasCompleted() {
        // Given
        SingleJobRunner singleJobRunner = new SingleJobRunner();
        AtomicBoolean firstJobRan = new AtomicBoolean(false);
        singleJobRunner.run(() -> firstJobRan.set(true), throwable -> {
        });
        assertThat(firstJobRan::get, eventuallySatisfies(is(equalTo(true)))
                .within(100, MILLISECONDS, checkingEvery(10, MILLISECONDS)));

        AtomicBoolean secondJobRan = new AtomicBoolean(false);

        // When
        singleJobRunner.run(() -> secondJobRan.set(true), throwable -> {
        });

        // Then
        assertThat(secondJobRan::get, eventuallySatisfies(is(equalTo(true)))
                .within(100, MILLISECONDS, checkingEvery(10, MILLISECONDS)));
    }

    @Test
    public void shouldInvokeExceptionHandlerWithExceptionThrownByJob() {
        // Given
        SingleJobRunner singleJobRunner = new SingleJobRunner();
        RuntimeException exceptionThrownByJob = new RuntimeException();
        List<Throwable> throwablesPassedToConsumer = new CopyOnWriteArrayList<>();

        // When
        singleJobRunner.run(() -> {
            throw exceptionThrownByJob;
        }, throwablesPassedToConsumer::add);

        // Then
        assertThat(() -> throwablesPassedToConsumer, eventuallySatisfies(hasSize(1))
                .within(100, MILLISECONDS, checkingEvery(10, MILLISECONDS)));
        assertThat(throwablesPassedToConsumer.get(0).getCause(), is(equalTo(exceptionThrownByJob)));
    }

    @Test
    public void shouldRunAJobThatIsProvidedAfterAnExceptionWasThrownByAJob() {
        // Given
        SingleJobRunner singleJobRunner = new SingleJobRunner();
        RuntimeException exceptionThrownByJob = new RuntimeException();
        List<Throwable> throwablesPassedToConsumer = new CopyOnWriteArrayList<>();

        singleJobRunner.run(() -> {
            throw exceptionThrownByJob;
        }, throwablesPassedToConsumer::add);

        assertThat(() -> throwablesPassedToConsumer, eventuallySatisfies(hasSize(1))
                .within(100, MILLISECONDS, checkingEvery(10, MILLISECONDS)));

        AtomicBoolean secondJobRan = new AtomicBoolean(false);

        // When
        singleJobRunner.run(() -> secondJobRan.set(true), throwable -> {
        });

        // Then
        assertThat(secondJobRan::get, eventuallySatisfies(is(equalTo(true)))
                .within(100, MILLISECONDS, checkingEvery(10, MILLISECONDS)));
    }

    @Test
    public void shouldInvokeExceptionHandlerWithWrappedCheckedExceptionThrownByJob() {
        // Given
        SingleJobRunner singleJobRunner = new SingleJobRunner();
        Exception exceptionThrownByJob = new Exception();
        List<Throwable> throwablesPassedToConsumer = new CopyOnWriteArrayList<>();

        // When
        singleJobRunner.run(() -> {
            throw exceptionThrownByJob;
        }, throwablesPassedToConsumer::add);

        // Then
        assertThat(() -> throwablesPassedToConsumer, eventuallySatisfies(hasSize(1))
                .within(100, MILLISECONDS, checkingEvery(10, MILLISECONDS)));
        assertThat(throwablesPassedToConsumer.get(0).getCause().getCause(), is(equalTo(exceptionThrownByJob)));
    }

    @Test
    public void shouldRunProvidedRunnableInBackground() {
        // Given
        AtomicBoolean jobRan = new AtomicBoolean(false);
        SingleJobRunner singleJobRunner = new SingleJobRunner();

        // When
        boolean jobAccepted = singleJobRunner.run(
                () -> {
                    Thread.sleep(1000);
                    jobRan.set(true);
                },
                throwable -> {
                });

        // Then
        assertThat(jobAccepted, is(equalTo(true)));
        assertThat(jobRan.get(), is(equalTo(false)));
        assertThat(jobRan::get, eventuallySatisfies(is(equalTo(true)))
                .within(2, SECONDS, checkingEvery(100, MILLISECONDS)));
    }

    @Test
    public void shouldOnlyRunOneJobAtATime() {
        // Given
        AtomicBoolean firstJobRan = new AtomicBoolean(false);
        AtomicBoolean secondJobRan = new AtomicBoolean(false);
        SingleJobRunner singleJobRunner = new SingleJobRunner();

        // When
        boolean firstJobAccepted = singleJobRunner.run(
                () -> {
                    Thread.sleep(1000);
                    firstJobRan.set(true);
                },
                throwable -> {
                });
        boolean secondJobAccepted = singleJobRunner.run(
                () -> secondJobRan.set(true),
                throwable -> {
                });

        // Then
        assertThat(firstJobAccepted, is(equalTo(true)));
        assertThat(firstJobRan::get, eventuallySatisfies(is(equalTo(true)))
                .within(2, SECONDS, checkingEvery(100, MILLISECONDS)));
        assertThat(secondJobAccepted, is(equalTo(false)));
        assertThat(secondJobRan.get(), is(equalTo(false)));
    }

}