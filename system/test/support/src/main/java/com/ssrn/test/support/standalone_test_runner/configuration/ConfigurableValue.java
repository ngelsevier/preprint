package com.ssrn.test.support.standalone_test_runner.configuration;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import java.util.function.Function;

public class ConfigurableValue<T> extends ConfigurableParameterBase<T> {

    private final String commandLineArgumentName;
    private final Function<String, T> parser;
    private final String commandLineArgumentValueDisplayName;

    public ConfigurableValue(String commandLineArgumentName, Function<String, T> parser, String defaultValue, String commandLineArgumentValueDisplayName, String description) {
        super(parser, commandLineArgumentName, defaultValue, description);
        this.commandLineArgumentName = commandLineArgumentName;
        this.parser = parser;
        this.commandLineArgumentValueDisplayName = commandLineArgumentValueDisplayName;
    }

    @Override
    protected void customize(Option.Builder optionBuilder) {
        optionBuilder.hasArg().argName(commandLineArgumentValueDisplayName);
    }

    @Override
    protected T getValueSpecifiedIn(CommandLine commandLine) {
        return parser.apply(commandLine.getOptionValue(commandLineArgumentName));
    }
}
