package ru.spbau.mit.utils;

import ru.spbau.mit.commands.*;

import java.util.ArrayList;

public class CommandFactory {
    public static Command getCommand(RawCommandData rawCommand) {
        switch (rawCommand.getCommandName()) {
            case "cat":
                return new Cat(rawCommand.getArguments());
            case "pwd":
                return new Pwd(rawCommand.getArguments());
            case "echo":
                return new Echo(rawCommand.getArguments());
            case "wc":
                return new Wc(rawCommand.getArguments());
            case "Assignment":
                return new Assignment(rawCommand.getArguments());
            case "exit":
                return new Exit(rawCommand.getArguments());
            default:
                ArrayList<String> args = new ArrayList<>();
                args.add(rawCommand.getCommandName());
                args.addAll(rawCommand.getArguments());
                return new ExternalProcess(args);
        }
    }
}
