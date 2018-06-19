package com.ssrn.test.support.standalone_test_runner.configuration;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.util.function.Function;

import static com.ssrn.test.support.configuration.EnvironmentVariableUtils.getEnvironmentVariableOrDefaultTo;

public abstract class ConfigurableParameterBase<T> implements ConfigurableParameter<T> {
    private final String commandLineArgumentName;
    private final String description;
    private final T environmentVariableValue;
    private T commandLineValue;

    ConfigurableParameterBase(Function<String, T> parser, String commandLineArgumentName, String defaultValue, String description) {
        this.commandLineArgumentName = commandLineArgumentName;
        this.description = description;
        environmentVariableValue = parser.apply(getEnvironmentVariableOrDefaultTo(defaultValue, environmentVariableFormOf(commandLineArgumentName)));
    }

    private static String environmentVariableFormOf(String commandLineArgumentName) {
        return commandLineArgumentName.replace("-", "_").toUpperCase();
    }

    @Override
    public void overrideIfSpecifiedIn(CommandLine commandLine) {
        if (commandLine.hasOption(commandLineArgumentName)) {
            commandLineValue = getValueSpecifiedIn(commandLine);
        }
    }

    @Override
    public T get() {
        return commandLineValue == null ? environmentVariableValue : commandLineValue;
    }

    @Override
    public String getCommandLineArgumentName() {
        return commandLineArgumentName;
    }

    @Override
    public void addOptionTo(Options options) {
        Option.Builder optionBuilder = Option.builder()
                .longOpt(commandLineArgumentName)
                .desc(description);

        customize(optionBuilder);

        options.addOption(optionBuilder.build());
    }

    protected abstract T getValueSpecifiedIn(CommandLine commandLine);

    protected void customize(Option.Builder optionBuilder) {
    }
}
