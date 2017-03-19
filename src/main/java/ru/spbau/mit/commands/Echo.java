package ru.spbau.mit.commands;

import java.io.*;
import java.util.ArrayList;

/**
* The {@code Echo} class implements the command that prints its arguments
 * with a space delimeter to its standard output.
*/

public class Echo implements Command {
    private ArrayList<String> arguments;

    public Echo(ArrayList<String> args) {
        arguments = args;
    }

    /**
     * Prints arguments to its stdout, ignores standard input.
     */

    @Override
    public OutputStream run(InputStream in) throws IOException {
        OutputStream out = new ByteArrayOutputStream();
        BufferedWriter outWriter = new BufferedWriter(new OutputStreamWriter(out));

        String delimiter = "";
        for (String arg : arguments) {
            outWriter.write(delimiter);
            delimiter = " ";
            outWriter.write(arg);
        }

        outWriter.flush();
        return out;
    }
}
