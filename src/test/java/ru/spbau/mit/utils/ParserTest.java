package ru.spbau.mit.utils;

import org.junit.Test;

import java.text.ParseException;
import java.util.Queue;

import static org.junit.Assert.*;

public class ParserTest {
    private static Parser parser = new Parser();
    private static Environment environment = Environment.getInstance();

    @Test
    public void parsePiped() throws Exception {
        String inputData = "echo 123 | wc";
        Queue<RawCommandData> commands = parser.parse(inputData);

        assertEquals(2, commands.size());
        RawCommandData rawCommand = commands.remove();
        assertTrue(rawCommand.getCommandName().equals("echo"));
        assertTrue(rawCommand.getArguments().get(0).equals("123"));
        rawCommand = commands.remove();
        assertTrue(rawCommand.getCommandName().equals("wc"));
        assertEquals(0, rawCommand.getArguments().size());
    }

    @Test
    public void parseSubstitution() throws Exception {
        environment.updateEnv("x", "5");
        environment.updateEnv("longVAR", "command args");
        String inputSimple = "$x";
        String inputComplex = "$longVAR";

        Queue<RawCommandData> commands = parser.parse(inputSimple);
        assertEquals(1, commands.size());
        RawCommandData rawCommand = commands.remove();
        assertTrue(rawCommand.getCommandName().equals("5"));
        assertEquals(0, rawCommand.getArguments().size());

        commands = parser.parse(inputComplex);
        assertEquals(1, commands.size());
        rawCommand = commands.remove();
        assertTrue(rawCommand.getCommandName().equals("command"));
        assertEquals(1, rawCommand.getArguments().size());
    }

    @Test
    public void parseAssignment() throws Exception {
        environment.updateEnv("x", "5");
        String inputSimple = "x=5";
        String inputWithoutSecondArg = "x=";
        String inputNoSuchValue = "x=$y";
        String inputSubstituted = "y=$x";
        String inputWithComplexSubstitution = "z=\"$x 123 word\"";
        String inputWithMoreArgs = "z=x 123 word";
        String inputWithMoreEqualities = "z=x=y";

        Queue<RawCommandData> commands = parser.parse(inputSimple);
        assertEquals(1, commands.size());
        RawCommandData rawCommand = commands.remove();
        assertTrue(rawCommand.getCommandName().equals("Assignment"));
        String firstArgument = rawCommand.getArguments().get(0);
        assertTrue(firstArgument.equals("x"));
        String secondArgument = rawCommand.getArguments().get(1);
        assertTrue(secondArgument.equals("5"));

        boolean exception = false;
        try {
            parser.parse(inputWithoutSecondArg);
        } catch (Exception e) {
            if (e instanceof ParseException) {
                exception = true;
            }
        }
        assertTrue(exception);

        exception = false;
        try {
            parser.parse(inputNoSuchValue);
        } catch (Exception e) {
            if (e instanceof ParseException) {
                exception = true;
            }
        }
        assertTrue(exception);

        commands = parser.parse(inputSubstituted);
        assertEquals(1, commands.size());
        rawCommand = commands.remove();
        assertTrue(rawCommand.getCommandName().equals("Assignment"));
        firstArgument = rawCommand.getArguments().get(0);
        assertTrue(firstArgument.equals("y"));
        secondArgument = rawCommand.getArguments().get(1);
        assertTrue(secondArgument.equals("5"));

        commands = parser.parse(inputWithComplexSubstitution);
        assertEquals(1, commands.size());
        rawCommand = commands.remove();
        assertTrue(rawCommand.getCommandName().equals("Assignment"));
        firstArgument = rawCommand.getArguments().get(0);
        assertTrue(firstArgument.equals("z"));
        secondArgument = rawCommand.getArguments().get(1);
        assertTrue(secondArgument.equals("5 123 word"));

        commands = parser.parse(inputWithMoreArgs);
        assertEquals(1, commands.size());
        rawCommand = commands.remove();
        assertTrue(rawCommand.getCommandName().equals("Assignment"));
        assertEquals(2, rawCommand.getArguments().size());
        firstArgument = rawCommand.getArguments().get(0);
        assertTrue(firstArgument.equals("z"));
        secondArgument = rawCommand.getArguments().get(1);
        assertTrue(secondArgument.equals("x"));

        exception = false;
        try {
            parser.parse(inputWithMoreEqualities);
        } catch (Exception e) {
            if (e instanceof ParseException) {
                exception = true;
            }
        }
        assertTrue(exception);
    }

    @Test
    public void parseQuotes() throws Exception {
        environment.updateEnv("x", "5");
        String inputQuotedSubstitution = "cat \"$x\"";
        String inputWrongQuotedSubstitution = "cat \"$x";
        String inputQuotedNoValue = "cat \"$y\"";
        String inputWeakInsideFullQuotes = "cat \"\'$x\'\"";
        String inputFullQuotes = "cat \"word $x 123\"";
        String inputWeakQuotes = "cat \'word $x 123\'";
        String inputTwoTypesOfQuoting = "cat \"word $x 123\" \'word $x 123\'";

        Queue<RawCommandData> commands = parser.parse(inputQuotedSubstitution);
        assertEquals(1, commands.size());
        RawCommandData rawCommand = commands.remove();
        assertTrue(rawCommand.getCommandName().equals("cat"));
        assertEquals(1, rawCommand.getArguments().size());
        String firstArgument = rawCommand.getArguments().get(0);
        assertTrue(firstArgument.equals("5"));

        boolean exception = false;
        try {
            parser.parse(inputWrongQuotedSubstitution);
        } catch (Exception e) {
            if (e instanceof ParseException) {
                exception = true;
            }
        }
        assertTrue(exception);

        exception = false;
        try {
            parser.parse(inputQuotedNoValue);
        } catch (Exception e) {
            if (e instanceof ParseException) {
                exception = true;
            }
        }
        assertTrue(exception);

        commands = parser.parse(inputWeakInsideFullQuotes);
        assertEquals(1, commands.size());
        rawCommand = commands.remove();
        assertTrue(rawCommand.getCommandName().equals("cat"));
        assertEquals(1, rawCommand.getArguments().size());
        firstArgument = rawCommand.getArguments().get(0);
        assertTrue(firstArgument.equals("\'5\'"));

        commands = parser.parse(inputFullQuotes);
        assertEquals(1, commands.size());
        rawCommand = commands.remove();
        assertTrue(rawCommand.getCommandName().equals("cat"));
        assertEquals(1, rawCommand.getArguments().size());
        firstArgument = rawCommand.getArguments().get(0);
        assertTrue(firstArgument.equals("word 5 123"));

        commands = parser.parse(inputWeakQuotes);
        assertEquals(1, commands.size());
        rawCommand = commands.remove();
        assertTrue(rawCommand.getCommandName().equals("cat"));
        assertEquals(1, rawCommand.getArguments().size());
        firstArgument = rawCommand.getArguments().get(0);
        assertTrue(firstArgument.equals("word $x 123"));

        commands = parser.parse(inputTwoTypesOfQuoting);
        assertEquals(1, commands.size());
        rawCommand = commands.remove();
        assertTrue(rawCommand.getCommandName().equals("cat"));
        assertEquals(2, rawCommand.getArguments().size());
        firstArgument = rawCommand.getArguments().get(0);
        assertTrue(firstArgument.equals("word 5 123"));
        String secondArgument = rawCommand.getArguments().get(1);
        assertTrue(secondArgument.equals("word $x 123"));
    }
}
