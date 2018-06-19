package com.ssrn.shared.dropwizard;

import io.dropwizard.lifecycle.Managed;

import java.util.function.Consumer;

public class AutoclosingManagedObject<T extends AutoCloseable> implements Managed {
    private final T autoCloseable;
    private final Consumer<T> autoCloseableStarter;

    public static <T extends AutoCloseable> AutoclosingManagedObject<T> managedObjectForAutocloseable(T autocloseable, Consumer<T> autocloseableConsumer) {
        return new AutoclosingManagedObject<>(autocloseable, autocloseableConsumer);
    }

    private AutoclosingManagedObject(T autoCloseable, Consumer<T> autoCloseableStarter) {
        this.autoCloseable = autoCloseable;
        this.autoCloseableStarter = autoCloseableStarter;
    }

    @Override
    public void start() throws Exception {
        autoCloseableStarter.accept(autoCloseable);
    }

    @Override
    public void stop() throws Exception {
        autoCloseable.close();
    }
}
