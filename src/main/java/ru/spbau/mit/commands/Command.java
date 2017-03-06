package ru.spbau.mit.commands;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
* The {@code Command} is the interface for abstract Shell Command
*/

public interface Command {

    /**
     * Method to run a specified command from Shell. Takes and returns Stream
     * so many commands could be chained via pipe
     * @param in - command's standard input
     * @return out - command's standard output
     */

    OutputStream run(InputStream in) throws IOException;
}
