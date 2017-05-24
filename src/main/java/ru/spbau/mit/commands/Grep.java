package ru.spbau.mit.commands;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.mit.utils.Environment;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The {@code Grep} class implements the command that prints lines that contain match for a user-specified pattern.
 */

public class Grep implements Command {
    private final List<String> arguments;
    private final Environment environment = Environment.getInstance();

    @Parameter(names = "-i", description="Case insensitivity.")
    private boolean caseInsensitive;

    @Parameter(names = "-w", description = "Search for whole words.")
    private boolean wholeWords;

    @Parameter(names="-A", description = "Number of lines to print after match.")
    private int numLinesAfterMatch;

    @Parameter(names="-f", converter = FileConverter.class, description = "Name of the file to grep. " +
            "Parses standard input if not provided.")
    private File file;

    @Parameter(required = true, description = "Regular expression pattern.")
    private List<String> pattern;

    public Grep(List<String> args) {
        arguments = args;
    }

    /**
     * Runs grep command
     * @param in - command's standard input
     * @return output stream that contains lines with match for user-specified pattern
     * @throws IOException - if -f key was provided, but interacting with file throwed IOException or if
     * an exception working with IOStreams occurred
     */
    @Override
    public OutputStream run(InputStream in) throws IOException {
        parseArgs();
        OutputStream out = new ByteArrayOutputStream();
        BufferedWriter outWriter = new BufferedWriter(new OutputStreamWriter(out));
        String text = getText(in);
        List<String> matches = grep(text);

        for (String match : matches) {
            outWriter.write(match);
        }

        outWriter.flush();
        return out;
    }

    /**
     *
     * @param text - text to search pattern in
     * @return List of matches for the pattern in text (and @code{numLinesAfterMatch} lines after each match)
     */
    private List<String> grep(String text) {
        List<String> result = new ArrayList<>();
        String[] lines = text.split("\\r\\n|\\r|\\n");
        Pattern p = getPatternFromUserSettings();

        int i = 0;
        while(i < lines.length) {
            String greppedLine = grepInLine(p, lines[i]);
            if (!greppedLine.equals(lines[i])) {
                result.add(greppedLine + "\n");
                for (int j = i + 1; j < i + 1 + numLinesAfterMatch && j < lines.length; j++) {
                    result.add(grepInLine(p, lines[j]) + "\n");
                }
                result.add("--\n");
                i += numLinesAfterMatch;
            }
            i += 1;
        }

        return result;
    }

    /**
     * Compiles a pattern by following rules:
     * 1. If @code{caseInsensitive} is true then compiles a pattern with flags CASE_INSENSITIVE and UNICODE_CASE
     * 2. If @code{wholeWords} is true then surrounds user-specified pattern with "\\b"
     * @return Java Pattern compiled from user pattern by rules above
     */
    @NotNull
    private Pattern getPatternFromUserSettings() {
        Pattern p;
        String userPattern = String.join(" ", pattern);

        if (wholeWords) {
            userPattern = "\\b" + userPattern + "\\b";
        }
        if (caseInsensitive) {
            p = Pattern.compile(userPattern, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        } else {
            p = Pattern.compile(userPattern);
        }

        return p;
    }

    /**
     *
     * @param p - Pattern to look for in line
     * @param line - line to search pattern in
     * @return - line with highlighted match
     */
    @NotNull
    private String grepInLine(Pattern p, String line) {
        Matcher  m = p.matcher(line);
        while (m.find()) {
            String found = m.group();
            line = line.replace(found, highlight(found));
        }
        return line;
    }

    /**
     * Determines a way to highlight string
     * @param toHighlight - string to apply highlighting to
     * @return - highlighted string
     */
    @NotNull
    private String highlight(@Nullable String toHighlight) {
       final String ANSI_RED = "\u001B[31m";
       final String ANSI_RESET = "\u001B[0m";
       return ANSI_RED + toHighlight + ANSI_RESET;
    }

    /**
     * Function to get text to apply grep to. If -f key was provided so @code{file} is not null, then reads text from
     * file, otherwise gets text from Command's standard input
     * @param in - Command's standard input
     * @return - text from file or standard input
     */
    private String getText(InputStream in) throws IOException {
        if (file != null) {
            return FileUtils.readFileToString(file);
        } else {
            String res = IOUtils.toString(in);
            IOUtils.closeQuietly(in);
            return res;
        }
    }

    /**
     * Parses @code{arguments}
     */
    private void parseArgs() throws IOException {
        JCommander jc = JCommander.newBuilder()
                .addObject(this)
                .build();
        try {
            jc.parse(arguments.toArray(new String[arguments.size()]));
        } catch (Throwable e) {
            jc.usage();
            throw new IOException("", e);
        }
    }
}
