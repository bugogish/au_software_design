package ru.spbau.mit.commands;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class CatTest {
    @Test(expected=FileNotFoundException.class)
    public void testFileNonExistence() throws Exception {
        String filepath = "someFilenameThatDoesNotExist";
        Path path = Paths.get(filepath);
        ArrayList<String> args = new ArrayList<>();
        args.add(filepath);
        Command operation = new Cat(args);

        if (Files.exists(path)) {
            fail();
        }

        operation.run(System.in);
    }

    @Test
    public void testReadingFromFile() throws Exception {
        String filepath = "someFilename";
        String fileContent = "test\ntestTEST";
        Path path = Paths.get(filepath);
        if (Files.exists(path)) {
            fail();
        }
        Files.write(path, fileContent.getBytes());

        ArrayList<String> args = new ArrayList<>();
        args.add(filepath);
        Command command = new Cat(args);

        ByteArrayOutputStream out = (ByteArrayOutputStream) command.run(System.in);

        assertEquals(fileContent, out.toString());

        Files.delete(path);
    }

    @Test
    public void testReadingFromStream() throws Exception {
        String streamContent = "test\ntestTEST";

        Command operation = new Cat(new ArrayList<String>());
        OutputStream out = operation.run(new ByteArrayInputStream(streamContent.getBytes()));

        assertEquals(streamContent, out.toString());
    }
}
