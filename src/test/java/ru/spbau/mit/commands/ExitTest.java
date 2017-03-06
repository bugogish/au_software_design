package ru.spbau.mit.commands;

import org.junit.Test;
import ru.spbau.mit.Shell;

import static org.junit.Assert.assertTrue;

public class ExitTest {
    @Test
    public void testExit() throws Exception {
        Command operation = new Exit(null);
        operation.run(System.in);

        assertTrue(Shell.getExitRequest());
    }
}
