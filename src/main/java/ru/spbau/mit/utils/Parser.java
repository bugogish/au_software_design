package ru.spbau.mit.utils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
* The {@code Parser} class is a utility class for processing user's input.
*/

public class Parser {
    private enum ParserState {
        BEGIN,
        FULL_QUOTING,
        WEAK_QUOTING,
        SUBSTITUTION
    }
    private Environment environment = Environment.getInstance();

    /**
    * Parses an input String into a Queue of {@code RawCommandData}.
    * input string is splitted by "|" - symbol so it's not possible to use "|" inside any type of quotes.
    */

    public Queue<RawCommandData> parse(String input) throws ParseException {
        Queue<RawCommandData> result = new LinkedList<>();
        String[] rawCommands = input.split("\\|");

        for (String command : rawCommands) {
            if (command.contains("$")) {
                command = substituteFromEnvironment(command);
            }
            if (command.contains("=")) {
                result.add(parseAssignment(command));
            } else {
                result.add(parseCommand(command));
            }
        }

        return result;
    }

    private ArrayList<String> splitInput(String input) {
        ArrayList<String> splitted = new ArrayList<>();
        ParserState state = ParserState.BEGIN;
        StringBuilder argName = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char currentSymbol = input.charAt(i);
            switch (state) {
                case BEGIN:
                    if (currentSymbol == '\"') {
                        state = ParserState.FULL_QUOTING;
                    } else if (currentSymbol == '\'') {
                        state = ParserState.WEAK_QUOTING;
                    } else if (Character.isWhitespace(currentSymbol)) {
                        if (argName.length() > 0) {
                            splitted.add(argName.toString());
                            argName.setLength(0);
                        }
                    } else {
                        argName.append(currentSymbol);
                        if (i == input.length() - 1 && argName.length() > 0) {
                                splitted.add(argName.toString());
                                argName.setLength(0);
                        }
                    }
                    break;
                case WEAK_QUOTING:
                    if (currentSymbol == '\'') {
                        splitted.add(argName.toString());
                        argName.setLength(0);
                        state = ParserState.BEGIN;
                    } else {
                        argName.append(currentSymbol);
                    }
                    break;
                case FULL_QUOTING:
                    if (currentSymbol == '\"') {
                        splitted.add(argName.toString());
                        argName.setLength(0);
                        state = ParserState.BEGIN;
                    } else {
                        argName.append(currentSymbol);
                    }
            }
        }

        return splitted;
    }

    private RawCommandData parseCommand(String command) {
        RawCommandData rawCommand = new RawCommandData();
        ArrayList<String> nameAndArgs = splitInput(command);

        rawCommand.setCommandName(nameAndArgs.get(0));
        nameAndArgs.remove(0);
        rawCommand.getArguments().addAll(nameAndArgs);

        return rawCommand;
    }

    private RawCommandData parseAssignment(String command) throws ParseException {
        RawCommandData rawCommand = new RawCommandData();
        String[] nameAndArgs = command.split("=");

        if (nameAndArgs.length != 2) {
            throw new ParseException("Wrong assignment format.", 0);
        }
        rawCommand.setCommandName("Assignment");
        rawCommand.addArgument(nameAndArgs[0]);
        ArrayList<String> otherArgs = splitInput(nameAndArgs[1]);
        rawCommand.addArgument(otherArgs.get(0));

        return rawCommand;
    }

    private String substituteFromEnvironment(String command) throws ParseException {
        StringBuilder result = new StringBuilder();
        ParserState state = ParserState.BEGIN;
        StringBuilder substitutionKey = new StringBuilder();
        boolean isSubstFromQuotes = false;

        for (int i = 0; i < command.length(); i++) {
            switch (state) {
                case BEGIN:
                    if (command.charAt(i) == '\'') {
                        state = ParserState.WEAK_QUOTING;
                        result.append(command.charAt(i));
                    } else if (command.charAt(i) == '\"') {
                        state = ParserState.FULL_QUOTING;
                        result.append(command.charAt(i));
                    } else if (command.charAt(i) == '$') {
                        state = ParserState.SUBSTITUTION;
                    } else {
                        result.append(command.charAt(i));
                    }

                    break;
                case WEAK_QUOTING:
                    if (command.charAt(i) == '\'') {
                        state = ParserState.BEGIN;
                    }
                    result.append(command.charAt(i));
                    break;
                case FULL_QUOTING:
                    if (command.charAt(i) == '$') {
                        state = ParserState.SUBSTITUTION;
                        isSubstFromQuotes = true;
                    } else if (command.charAt(i) == '\"') {
                        state = ParserState.BEGIN;
                        isSubstFromQuotes = false;
                        result.append(command.charAt(i));
                    } else {
                        result.append(command.charAt(i));
                    }
                    break;
                case SUBSTITUTION:
                    if (isSubstFromQuotes && i != command.length() - 1 && command.charAt(i + 1) == '\"') {
                        if (command.charAt(i) != '\'') {
                            substitutionKey.append(command.charAt(i));
                        }
                        String value = environment.getVarValue(substitutionKey.toString());
                        if (value == null) {
                            throw new ParseException(substitutionKey + " value is not set.", i);
                        }
                        result.append(value);
                        if (command.charAt(i) == '\'') {
                            result.append("\'");
                        }
                        substitutionKey.setLength(0);
                        state = ParserState.FULL_QUOTING;
                    } else if (Character.isWhitespace(command.charAt(i)) || i == command.length() - 1
                            || command.charAt(i) == '\'') {
                        if (i == command.length() - 1 && !(Character.isWhitespace(command.charAt(i)))) {
                            substitutionKey.append(command.charAt(i));
                        }
                        String value =
                                environment.getVarValue(substitutionKey.toString());
                        if (value == null) {
                            throw new ParseException(substitutionKey + " value is not set.", i);
                        }
                        result.append(value);
                        if (Character.isWhitespace(command.charAt(i)) || command.charAt(i) == '\'') {
                            result.append(command.charAt(i));
                        }

                        substitutionKey.setLength(0);
                        if (isSubstFromQuotes) {
                            state = ParserState.FULL_QUOTING;
                        } else {
                            state = ParserState.BEGIN;
                        }
                    } else {
                        substitutionKey.append(command.charAt(i));
                    }
                    break;
            }
        }
        if (state != ParserState.BEGIN) {
            throw new ParseException("The specified expression has wrong format"
                    + " (Probably, mismatched brackets).", 0);
        }
        return result.toString();
    }
}
