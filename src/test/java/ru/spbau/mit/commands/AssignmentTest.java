package ru.spbau.mit.commands;

import org.junit.Test;
import ru.spbau.mit.utils.Environment;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

public class AssignmentTest {
    @Test
    public void testAssignment() throws Exception {
        ArrayList<String> args = new ArrayList<>();
        args.add("x");
        args.add("5");
        Command command = new Assignment(args);
        command.run(System.in);

        Environment environment = Environment.getInstance();
        assertTrue(environment.getVarValue("x").equals("5"));
    }
}
