package ru.spbau.mit.commands;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class GrepTest {
    private final static String END_OF_MATCH = "\n--\n";

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test(expected = IOException.class)
    public void testPatternNotSpecified() throws Exception {
        String streamContent = "test";

        Command operation = new Grep(new ArrayList<>());
        operation.run(new ByteArrayInputStream(streamContent.getBytes()));
    }

    @Test
    public void testReadingFromFile() throws Exception {
        String fileName = "someFilename";
        File file = testFolder.newFile(fileName);

        String fileContent = "123test321";
        String expectedResult = fileContent.replace("test", Grep.highlight("test")) + END_OF_MATCH;

        Files.write(Paths.get(file.getPath()), fileContent.getBytes());

        ArrayList<String> args = new ArrayList<>();
        args.add("-f");
        args.add(file.getAbsolutePath());
        args.add("test");

        Command operation = new Grep(args);
        OutputStream out = operation.run(System.in);

        assertEquals(expectedResult, out.toString());
    }

    @Test
    public void testReadingFromStream() throws Exception {
        String streamContent = "123test321";
        String expectedResult = streamContent.replace("test", Grep.highlight("test")) + END_OF_MATCH;

        ArrayList<String> args = new ArrayList<>();
        args.add("test");

        Command operation = new Grep(args);
        OutputStream out = operation.run(new ByteArrayInputStream(streamContent.getBytes()));

        assertEquals(expectedResult, out.toString());
    }

    @Test
    public void testLinesAfterMatch() throws Exception {
        String streamContent = "123test321\nline\nsecond line";
        String expectedResult = streamContent.replace("test",  Grep.highlight("test")) + END_OF_MATCH;

        ArrayList<String> args = new ArrayList<>();
        args.add("-A");
        args.add("4");
        args.add("test");

        Command operation = new Grep(args);
        OutputStream out = operation.run(new ByteArrayInputStream(streamContent.getBytes()));

        assertEquals(expectedResult, out.toString());
    }

    @Test
    public void testCaseInsensitive() throws Exception {
        String streamContent = "123teSt321";
        String expectedResult = streamContent.replace("teSt",  Grep.highlight("teSt")) + END_OF_MATCH;

        ArrayList<String> args = new ArrayList<>();
        args.add("-i");
        args.add("tEst");

        Command operation = new Grep(args);
        OutputStream out = operation.run(new ByteArrayInputStream(streamContent.getBytes()));

        assertEquals(expectedResult, out.toString());
    }

    @Test
    public void testWholeWords() throws Exception {
        String streamContent = "123test321 test";
        String expectedResult = "123test321 " + Grep.highlight("test") + END_OF_MATCH;

        ArrayList<String> args = new ArrayList<>();
        args.add("-w");
        args.add("test");

        Command operation = new Grep(args);
        OutputStream out = operation.run(new ByteArrayInputStream(streamContent.getBytes()));

        assertEquals(expectedResult, out.toString());
    }

    @Test
    public void testRegExp() throws Exception {
        String streamContent = "123test321 test";
        String expectedResult = streamContent.replace("test321",  Grep.highlight("test321")) + END_OF_MATCH;

        ArrayList<String> args = new ArrayList<>();
        args.add("\\D+\\d+");

        Command operation = new Grep(args);
        OutputStream out = operation.run(new ByteArrayInputStream(streamContent.getBytes()));

        assertEquals(expectedResult, out.toString());
    }
}
