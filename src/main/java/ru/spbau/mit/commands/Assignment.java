package ru.spbau.mit.commands;

import ru.spbau.mit.utils.Environment;

import java.io.*;
import java.util.ArrayList;

/**
* The {@code Assignment} class implements an internal Shell's command for updating environment with
* new variable-value pair provided by user by entering "variable=value" command.
*/

public class Assignment implements Command {
    private ArrayList<String> arguments;
    private Environment environment = Environment.getInstance();

    public Assignment(ArrayList<String> args) {
        arguments = args;
    }

    /**
     * Updates environment with values specified in arguments.
     * This command ignores its standard input and writes an empty value
     * to standard output, so it can be properly used in a pipe.
     */

    @Override
    public OutputStream run(InputStream in) throws IOException {
        OutputStream out = new ByteArrayOutputStream();
        BufferedWriter outWriter = new BufferedWriter(new OutputStreamWriter(out));

        outWriter.write("");
        environment.updateEnv(arguments.get(0), arguments.get(1));

        outWriter.flush();
        return out;
    }
}
