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

    @NotNull
    private Pattern getPatternFromUserSettings() {
        Pattern p;

        if (wholeWords) {
            pattern.set(0, "\\b" + pattern.get(0) + "\\b");
        }
        if (caseInsensitive) {
            p = Pattern.compile(pattern.get(0), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        } else {
            p = Pattern.compile(pattern.get(0));
        }

        return p;
    }

    @NotNull
    private String grepInLine(Pattern p, String line) {
        Matcher  m = p.matcher(line);
        while (m.find()) {
            String found = m.group();
            line = line.replace(found, highlight(found));
        }
        return line;
    }

    @NotNull
    private String highlight(@Nullable String toHighlight) {
        return "-> " + toHighlight + " <-";
    }

    private String getText(InputStream in) throws IOException {
        if (file != null) {
            return FileUtils.readFileToString(file);
        } else {
            String res = IOUtils.toString(in);
            IOUtils.closeQuietly(in);
            return res;
        }
    }

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
