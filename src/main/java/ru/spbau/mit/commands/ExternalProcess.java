package ru.spbau.mit.commands;

import java.io.*;
import java.util.ArrayList;

/**
* The {@code ExternalProcess} class is an internal shell Command for trying to launch an External Process.
* Instance is created when user inputs a command that is unknown to Shell.
*/

public class ExternalProcess implements Command {
    private ArrayList<String> arguments;

    public ExternalProcess(ArrayList<String> args) {
        arguments = args;
    }

    /**
     * Tries to launch an external process and redirect to
     * its InputStream everything from the standard input,
     * then redirects external processes's output to standard output.
     */

    @Override
    public OutputStream run(InputStream in) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(arguments);
        final Process process = pb.start();
        OutputStream out = new ByteArrayOutputStream();
        BufferedReader inReader = null;
        if (in != null) {
            inReader = new BufferedReader(new InputStreamReader(in));
        }
        OutputStream processOutputStream = process.getOutputStream();
        InputStream processInputStream = process.getInputStream();

        if (inReader != null) {
            try (BufferedWriter processWriter = new BufferedWriter(new OutputStreamWriter(processOutputStream))) {
                String line;
                while ((line = inReader.readLine()) != null) {
                    processWriter.write(line);
                }
                processWriter.flush();
                processWriter.close();
            } catch (IOException e) { }
        }

        try (BufferedReader processReader = new BufferedReader(new InputStreamReader(processInputStream))) {
            String line;
            while ((line = processReader.readLine()) != null) {
                out.write(line.getBytes());
                out.write('\n');
            }
        } catch (IOException e) { }

        out.flush();
        return out;
    }
}
