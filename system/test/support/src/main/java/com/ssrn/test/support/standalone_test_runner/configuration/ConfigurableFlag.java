package com.ssrn.test.support.standalone_test_runner.configuration;

import org.apache.commons.cli.CommandLine;

public class ConfigurableFlag extends ConfigurableParameterBase<Boolean> {

    public ConfigurableFlag(String commandLineArgumentName, String defaultValue, String description) {
        super(Boolean::valueOf, commandLineArgumentName, defaultValue, description);
    }

    @Override
    protected Boolean getValueSpecifiedIn(CommandLine commandLine) {
        return true;
    }
}
