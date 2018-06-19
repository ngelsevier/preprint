package com.ssrn.test.support.standalone_test_runner.configuration;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class ConfigurationBase {
    private final String commandLineArgumentPrefix;
    private final List<ConfigurableParameter> configurableParameters = new ArrayList<>();

    protected ConfigurationBase(String commandLineArgumentPrefix) {
        this.commandLineArgumentPrefix = commandLineArgumentPrefix;
    }

    public void addOptionsTo(Options options) {
        forEachConfigurableParameter(parameter -> parameter.addOptionTo(options));
    }

    public void overrideIfSpecifiedIn(CommandLine commandLine) {
        forEachConfigurableParameter(parameter -> parameter.overrideIfSpecifiedIn(commandLine));
    }

    protected void addParameter(String commandLineArgumentShortName, String defaultValue, String description) {
        configurableParameters.add(new ConfigurableFlag(
                commandLineArgumentPrefix + commandLineArgumentShortName,
                defaultValue,
                description
        ));
    }

    protected <T> void addParameter(String commandLineArgumentShortName, Function<String, T> parse, String defaultValue, String commandLineArgumentValueDisplayName, String description) {
        configurableParameters.add(new ConfigurableValue<>(
                commandLineArgumentPrefix + commandLineArgumentShortName,
                parse,
                defaultValue,
                commandLineArgumentValueDisplayName,
                description
        ));
    }

    @SuppressWarnings("unchecked")
    protected <T> T getValueOf(String commandLineArgumentShortName) {
        String commandLineArgumentName = commandLineArgumentPrefix + commandLineArgumentShortName;

        return (T) configurableParameters
                .stream()
                .filter(parameter -> commandLineArgumentName.equals(parameter.getCommandLineArgumentName()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("Unknown configurable parameter '%s'", commandLineArgumentName)))
                .get();
    }

    private void forEachConfigurableParameter(Consumer<ConfigurableParameter> actionToPerformOnConfigurableParameter) {
        configurableParameters.forEach(actionToPerformOnConfigurableParameter);
    }
}
