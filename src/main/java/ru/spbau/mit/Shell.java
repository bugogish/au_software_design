package ru.spbau.mit;
import ru.spbau.mit.commands.*;
import ru.spbau.mit.utils.CommandFactory;
import ru.spbau.mit.utils.ParseException;
import ru.spbau.mit.utils.Parser;
import ru.spbau.mit.utils.RawCommandData;

import java.io.*;
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
            Queue<Command> commandQueue = new LinkedList<>();;

            try {
                for (RawCommandData rawCommand : parser.parse(input)) {
                    commandQueue.add(CommandFactory.getCommand(rawCommand));
                }
            } catch (ParseException e) {
                System.out.println(e.getMessage());
                continue;
            }

            for (Command command : commandQueue) {
                try {
                    outputStream = command.run(inputStream);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    break;
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
}
