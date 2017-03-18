package ru.spbau.mit.commands;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class PwdTest {
    @Test
    public void testPWD() throws Exception {
        String inStreamContent = "";
        Command command = new Pwd(new ArrayList<>());
        OutputStream out = command.run(new ByteArrayInputStream(inStreamContent.getBytes()));

        assertEquals(System.getProperty("user.dir"), out.toString());
    }
}
