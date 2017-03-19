package ru.spbau.mit.commands;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
* The {@code Wc} class implements the command that outputs for its input:
  * 1. number of lines
  * 2. number of words that is determined by (\\W+) regular expression
  * 3. number of bytes
*/

public class Wc implements Command {
    private static final int BUFFER_SIZE = 1024;
    private ArrayList<String> arguments;

    public Wc(ArrayList<String> args) {
        arguments = args;
    }

    /**
     * Processes its arguments in the following way:
     * 1. if the argument to wc was specified then it attempts to open the file with name got from
     * {@code arguments[0]} (other arguments are ignored),
     * then processes the file in the way that will be described below.
     * 2. otherwise wc processes its standard input.
     * wc prints to its standard output the following information:
     * 1. number of lines
     * 2. number of words that is determined by (\\W+) regular expression
     * 3. number of bytes
     */

    @Override
    public OutputStream run(InputStream in) throws IOException {
        OutputStream out = new ByteArrayOutputStream();
        BufferedWriter outWriter = new BufferedWriter(new OutputStreamWriter(out));
        InputStream inReader;

        if (arguments.size() > 0) {
            String filename = arguments.get(0);
            Path path = Paths.get(filename);
            if (!Files.exists(path)) {
                throw new FileNotFoundException("File " + path + " does not exist.");
            }
            inReader = new FileInputStream(filename);
        } else {
            inReader = in;
        }

        if (inReader != null) {
            byte[] data = getBytes(inReader);
            String rawLines = new String(data);
            List<String> lines = Arrays.asList(rawLines.split("\\r\\n|\\r|\\n"));

            out.write(countLines(lines));
            out.write(' ');
            out.write(countWords(lines));
            out.write(' ');
            out.write(Integer.toString(data.length).getBytes());

            inReader.close();
        } else {
            throw new IOException("Input Stream is not correctly set.");
        }

        outWriter.flush();
        return out;
    }

    private byte[] getBytes(InputStream in) throws IOException {
        ByteArrayOutputStream outReader = new ByteArrayOutputStream();
        int len;
        byte[] data = new byte[BUFFER_SIZE];

        while ((len = in.read(data, 0, data.length)) != -1) {
            outReader.write(data, 0, len);
        }

        outReader.flush();
        return outReader.toByteArray();
    }

    private byte[] countLines(List<String> lines) {
        return Long.toString(lines.size()).getBytes();
    }

    private byte[] countWords(List<String> lines) throws IOException {
        int count = 0;

        for (String line : lines) {
            String[] words = line.split("\\W+");
            count += words.length;
        }

        return Integer.toString(count).getBytes();
    }
}
