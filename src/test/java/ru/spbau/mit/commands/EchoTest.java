package ru.spbau.mit.commands;

import org.junit.Test;

import java.io.OutputStream;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EchoTest {
    @Test
    public void testEcho() throws Exception {
        ArrayList<String> args = new ArrayList<>();
        args.add("test");
        args.add("test1 test2");
        args.add("TEST");

        Command operation = new Echo(args);
        OutputStream out = operation.run(System.in);

        assertEquals("test test1 test2 TEST", out.toString());
    }
}
