package com.ssrn.test.support.standalone_test_runner.configuration;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

public interface ConfigurableParameter<T> {
    void addOptionTo(Options options);

    void overrideIfSpecifiedIn(CommandLine commandLine);

    T get();

    String getCommandLineArgumentName();
}
