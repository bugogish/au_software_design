package ru.spbau.mit.commands;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class WcTest {

    private String correctResult(String input) {
        int numberOfLines = input.split("\\n|\\r\\n|\\r").length;
        int numberOfWords = input.split("\\W+").length;
        int numberOfBytes = input.getBytes().length;

        return numberOfLines + " " + numberOfWords + " " + numberOfBytes;
    }

    @Test
    public void countFromFileTest() throws Exception {
        String filepath = "someFilename";
        String fileContent = "test\ntestTEST";
        Path path = Paths.get(filepath);
        if (Files.exists(path)) {
            fail();
        }
        Files.write(path, fileContent.getBytes());
        String inStreamContent = "";

        ArrayList<String> args = new ArrayList<>();
        args.add(filepath);

        Command command = new Wc(args);
        OutputStream out = command.run(new ByteArrayInputStream(inStreamContent.getBytes()));

        assertEquals(correctResult(fileContent), out.toString());
        Files.delete(path);
    }

    @Test
    public void countFromInStreamTest() throws Exception {
        String streamContent = "test\ntestTEST";

        Command operation = new Wc(new ArrayList<>());
        OutputStream out = operation.run(new ByteArrayInputStream(streamContent.getBytes()));

        assertEquals(correctResult(streamContent), out.toString());
    }

}
