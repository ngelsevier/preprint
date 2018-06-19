package com.ssrn.test.support.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class SystemCommandUtils {
    public static void executeSystemCommand(String commandString) {
        Runtime runtime = Runtime.getRuntime();

        Process process;

        try {
            process = runtime.exec(commandString);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            process.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        int exitCode = process.exitValue();

        if (exitCode != 0) {
            BufferedReader standardOutReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader standardErrorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            throw new RuntimeException(String.format("System command '%s' exited with non-zero exit code: %d.\n\nStandard error was:\n\n%s\n\nStandard out was:\n\n%s",
                    commandString,
                    exitCode,
                    standardErrorReader.lines().collect(Collectors.joining("\n")),
                    standardOutReader.lines().collect(Collectors.joining("\n"))));
        }
    }
}
