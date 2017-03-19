package ru.spbau.mit.utils;

import org.junit.Test;
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
        assertEquals("echo", rawCommand.getCommandName());
        assertEquals("123", rawCommand.getArguments().get(0));

        rawCommand = commands.remove();
        assertEquals("wc", rawCommand.getCommandName());
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
        assertEquals("5", rawCommand.getCommandName());
        assertEquals(0, rawCommand.getArguments().size());

        commands = parser.parse(inputComplex);
        assertEquals(1, commands.size());
        rawCommand = commands.remove();
        assertEquals("command", rawCommand.getCommandName());
        assertEquals(1, rawCommand.getArguments().size());
    }

    @Test
    public void parseCorrectAssignment() throws Exception {
        environment.updateEnv("x", "5");
        String inputSimple = "x=5";
        String inputSubstituted = "y=$x";
        String inputWithComplexSubstitution = "z=\"$x 123 word\"";
        String inputWithMoreArgs = "z=x 123 word";

        Queue<RawCommandData> commands = parser.parse(inputSimple);
        assertEquals(1, commands.size());
        RawCommandData rawCommand = commands.remove();
        assertEquals("Assignment", rawCommand.getCommandName());
        String firstArgument = rawCommand.getArguments().get(0);
        assertEquals("x", firstArgument);
        String secondArgument = rawCommand.getArguments().get(1);
        assertEquals("5", secondArgument);

        commands = parser.parse(inputSubstituted);
        assertEquals(1, commands.size());
        rawCommand = commands.remove();
        assertEquals("Assignment", rawCommand.getCommandName());
        firstArgument = rawCommand.getArguments().get(0);
        assertEquals("y", firstArgument);
        secondArgument = rawCommand.getArguments().get(1);
        assertEquals("5", secondArgument);

        commands = parser.parse(inputWithComplexSubstitution);
        assertEquals(1, commands.size());
        rawCommand = commands.remove();
        assertEquals("Assignment", rawCommand.getCommandName());
        firstArgument = rawCommand.getArguments().get(0);
        assertEquals("z", firstArgument);
        secondArgument = rawCommand.getArguments().get(1);
        assertEquals("5 123 word", secondArgument);

        commands = parser.parse(inputWithMoreArgs);
        assertEquals(1, commands.size());
        rawCommand = commands.remove();
        assertEquals("Assignment", rawCommand.getCommandName());
        assertEquals(2, rawCommand.getArguments().size());
        firstArgument = rawCommand.getArguments().get(0);
        assertEquals("z", firstArgument);
        secondArgument = rawCommand.getArguments().get(1);
        assertEquals("x", secondArgument);
    }

    @Test(expected=ParseException.class)
    public void parseAssignmentWithoutSecondArgFails() throws Exception {
        environment.updateEnv("x", "5");
        String inputWithoutSecondArg = "x=";

        parser.parse(inputWithoutSecondArg);
    }

    @Test(expected=ParseException.class)
    public void parseAssignmentWithNoSuchValueFails() throws Exception {
        environment.updateEnv("x", "5");
        String inputNoSuchValue = "x=$y";

        parser.parse(inputNoSuchValue);

    }

    @Test(expected=ParseException.class)
    public void parseAssignmentWithMoreQualitiesFails() throws Exception {
        environment.updateEnv("x", "5");
        String inputWithMoreEqualities = "z=x=y";

        parser.parse(inputWithMoreEqualities);
    }

    @Test
    public void parseCorrectFullQuotes() throws ParseException {
        environment.updateEnv("x", "5");
        String inputQuotedSubstitution = "cat \"$x\"";
        String inputFullQuotes = "cat \"word $x 123\"";

        Queue<RawCommandData> commands = parser.parse(inputQuotedSubstitution);
        assertEquals(1, commands.size());
        RawCommandData rawCommand = commands.remove();
        assertEquals("cat", rawCommand.getCommandName());
        assertEquals(1, rawCommand.getArguments().size());
        String firstArgument = rawCommand.getArguments().get(0);
        assertEquals("5", firstArgument);

        commands = parser.parse(inputFullQuotes);
        assertEquals(1, commands.size());
        rawCommand = commands.remove();
        assertEquals("cat", rawCommand.getCommandName());
        assertEquals(1, rawCommand.getArguments().size());
        firstArgument = rawCommand.getArguments().get(0);
        assertEquals("word 5 123", firstArgument);
    }

    @Test(expected=ParseException.class)
    public void parseWrongFullQuotes() throws Exception {
        environment.updateEnv("x", "5");
        String inputWrongQuotedSubstitution = "cat \"$x";
        parser.parse(inputWrongQuotedSubstitution);
    }

    @Test(expected=ParseException.class)
    public void parseNoSavedValue() throws Exception {
        environment.updateEnv("x", "5");
        String inputQuotedNoValue = "cat \"$y\"";
        parser.parse(inputQuotedNoValue);
    }

    @Test
    public void parseWeakQuotes() throws Exception {
        environment.updateEnv("x", "5");
        String inputWeakQuotes = "cat \'word $x 123\'";

        Queue<RawCommandData> commands = parser.parse(inputWeakQuotes);
        assertEquals(1, commands.size());
        RawCommandData rawCommand = commands.remove();
        assertEquals("cat", rawCommand.getCommandName());
        assertEquals(1, rawCommand.getArguments().size());
        String firstArgument = rawCommand.getArguments().get(0);
        assertEquals("word $x 123", firstArgument);
    }

    @Test
    public void parseMixedQuotes() throws Exception {
        environment.updateEnv("x", "5");
        String inputWeakInsideFullQuotes = "cat \"\'$x\'\"";
        String inputTwoTypesOfQuoting = "cat \"word $x 123\" \'word $x 123\'";

        Queue<RawCommandData> commands = parser.parse(inputWeakInsideFullQuotes);
        assertEquals(1, commands.size());
        RawCommandData rawCommand = commands.remove();
        assertEquals("cat", rawCommand.getCommandName());
        assertEquals(1, rawCommand.getArguments().size());
        String firstArgument = rawCommand.getArguments().get(0);
        assertEquals("\'5\'", firstArgument);

        commands = parser.parse(inputTwoTypesOfQuoting);
        assertEquals(1, commands.size());
        rawCommand = commands.remove();
        assertEquals("cat", rawCommand.getCommandName());
        assertEquals(2, rawCommand.getArguments().size());
        firstArgument = rawCommand.getArguments().get(0);
        assertEquals("word 5 123", firstArgument);
        String secondArgument = rawCommand.getArguments().get(1);
        assertEquals("word $x 123", secondArgument);
    }
}
