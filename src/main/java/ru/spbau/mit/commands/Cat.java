package ru.spbau.mit.commands;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
* The {@code Cat} class implements the command that prints file or standard input to output.
*/

public class Cat implements Command {
    private static final int BUFFER_SIZE = 1024;
    private ArrayList<String> arguments;

    public Cat(ArrayList<String> args) {
        arguments = args;
    }

    /**
     * If an argument for cat was specified (so {@code arguments.size() > 0})
     * then prints the contents of the file with name stored in {@code arguments[0]}.
     * Otherwise, prints everything from its standard input to standard output.
     */

    @Override
    public OutputStream run(InputStream in) throws IOException {
        String filepath;
        OutputStream out = new ByteArrayOutputStream();

        if (arguments.size() > 0) {
            filepath = arguments.get(0);
            Path path = Paths.get(filepath);
            if (!Files.exists(path)) {
                throw new FileNotFoundException("File " + path + " does not exist.");
            }
            byte[] data = Files.readAllBytes(path);
            out.write(data);
        } else {
            if (in != null) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
            } else {
                throw new IOException("Input Stream is not correctly set.");
            }

        }

        out.flush();
        return out;
    }
}
