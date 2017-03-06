package ru.spbau.mit;
import jdk.nashorn.internal.runtime.ParserException;
import ru.spbau.mit.commands.*;
import ru.spbau.mit.utils.Parser;
import ru.spbau.mit.utils.RawCommandData;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

/**
* The {@code Shell} class controls the program workflow.
* It reads users input, preprocesses {RawCommandData} Queue from {@code Parser} (creates correct instances for
* {@code Command} classes) and then launches commands.
*/

public class Shell {
    private static boolean exitRequest = false;

    public static void setExitRequest() {
        exitRequest = true;
    }

    public static boolean getExitRequest() {
        return exitRequest;
    }
    
    /**
     * Starts Shell and executes commands entered by user.
     */
    
    public void run() throws Exception {
        exitRequest = false;
        Scanner in = new Scanner(System.in);
        Parser parser = new Parser();
        InputStream inputStream = null;
        OutputStream outputStream = null;

        while (in.hasNextLine()) {
            String input = in.nextLine();
            Queue<Command> commandQueue;

            try {
                commandQueue = preprocess(parser.parse(input));
            } catch (ParserException e) {
                System.out.println(e.getMessage());
                continue;
            }

            for (Command command : commandQueue) {
                try {
                    outputStream = command.run(inputStream);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    continue;
                }
                if (exitRequest) {
                    break;
                }
                inputStream = new ByteArrayInputStream(((ByteArrayOutputStream) outputStream).toByteArray());
            }
            if (exitRequest) {
                break;
            }
            if (outputStream != null) {
                System.out.println((outputStream).toString());
            }
        }

        System.out.println("Goodbye!");
    }

    private Queue<Command> preprocess(Queue<RawCommandData> commands) {
        Queue<Command> result = new LinkedList<>();

        for (RawCommandData command : commands) {
            switch (command.getCommandName()) {
                case "cat":
                    result.add(new Cat(command.getArguments()));
                    break;
                case "pwd":
                    result.add(new Pwd(command.getArguments())); // pwd HELLO WORLD
                    break;
                case "echo":
                    result.add(new Echo(command.getArguments()));
                    break;
                case "wc":
                    result.add(new Wc(command.getArguments()));
                    break;
                case "Assignment":
                    result.add(new Assignment(command.getArguments()));
                    break;
                case "exit":
                    result.add(new Exit(command.getArguments()));
                    break;
                default:
                    ArrayList<String> args = new ArrayList<>();
                    args.add(command.getCommandName());
                    args.addAll(command.getArguments());
                    result.add(new ExternalProcess(args));
                    break;
            }
        }

        return result;
    }
}
