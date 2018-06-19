package com.ssrn.test.support.standalone_test_runner;

import barrypitman.junitXmlFormatter.AntXmlRunListener;
import org.apache.commons.cli.*;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

public class TestRunner {

    public static void main(String[] args) {
        Options options = new Options();

        options.addOption(Option.builder("h")
                .longOpt("help")
                .desc("display help")
                .build());

        String testReportCommandLineOptionName = "test-report";

        options.addOption(Option.builder()
                .longOpt(testReportCommandLineOptionName)
                .hasArg()
                .desc("path to write an xUnit-style xml report to")
                .build());

        CommandLine commandLine = parseCommandLineArgumentsFor(options, args);

        if (commandLine.hasOption("help")) {
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp(150, "java -jar path/to/jar [options]", null, options, null);
            return;
        }

        JUnitCore jUnitCore = new JUnitCore();
        jUnitCore.addListener(new ConsoleOutputListener());

        if (commandLine.hasOption(testReportCommandLineOptionName)) {
            String testReportFilePath = commandLine.getOptionValue(testReportCommandLineOptionName);
            jUnitCore.addListener(new AntXmlRunListener(testReportFilePath));
        }

        Result result = jUnitCore.run(AutoScanningTestSuite.class);
        System.exit(result.wasSuccessful() ? 0 : 1);
    }

    private static CommandLine parseCommandLineArgumentsFor(Options options, String[] args) {
        DefaultParser defaultParser = new DefaultParser();

        try {
            return defaultParser.parse(options, args);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

}
