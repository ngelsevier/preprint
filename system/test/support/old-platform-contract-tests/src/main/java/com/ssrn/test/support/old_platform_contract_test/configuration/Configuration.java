package com.ssrn.test.support.old_platform_contract_test.configuration;

import com.ssrn.test.support.standalone_test_runner.configuration.ConfigurationBase;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.util.Arrays;
import java.util.function.Consumer;

public class Configuration {

    private static final CommandLineHttpClientConfiguration httpClientConfiguration = new CommandLineHttpClientConfiguration("http-client-");
    private static final CommandLineBrowserBasedTestingConfiguration browserBasedTestingConfiguration = new CommandLineBrowserBasedTestingConfiguration("browser-based-testing-");
    private static final CommandLineSsrnConfiguration ssrnConfiguration = new CommandLineSsrnConfiguration("ssrn-");

    private static final ConfigurationBase[] configurationBases = {
            httpClientConfiguration,
            browserBasedTestingConfiguration,
            ssrnConfiguration
    };

    public static CommandLineHttpClientConfiguration httpClient() {
        return httpClientConfiguration;
    }

    public static CommandLineBrowserBasedTestingConfiguration browserBasedTesting() {
        return browserBasedTestingConfiguration;
    }

    public static CommandLineSsrnConfiguration ssrn() {
        return ssrnConfiguration;
    }

    public static void addOptionsTo(Options options) {
        forEachConfiguration(configurationBase -> configurationBase.addOptionsTo(options));
    }

    public static void overrideIfSpecifiedIn(CommandLine commandLine) {
        forEachConfiguration(configurationBase -> configurationBase.overrideIfSpecifiedIn(commandLine));
    }

    private static void forEachConfiguration(Consumer<ConfigurationBase> actionToPerformWithConfguration) {
        Arrays.stream(configurationBases).forEach(actionToPerformWithConfguration);
    }
}
