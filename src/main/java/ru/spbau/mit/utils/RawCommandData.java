package ru.spbau.mit.utils;

import java.util.ArrayList;

/**
* {@code RawCommandData} is a wrapper class that contains description of a parsed command in the following format:
* {@code commandName} is shell command's or external process's name
* {@code arguments} is a List of possible command's arguments
*/

public class RawCommandData {
    private String commandName;
    private ArrayList<String> arguments = new ArrayList<>();

    /**
     * returns this RawCommand's name
     */

    public String getCommandName() {
        return commandName;
    }

    void addArgument(String arg) {
        arguments.add(arg);
    }

    void setCommandName(String cmd) {
        commandName = cmd;
    }

    /**
     * returns this RawCommand's arguments
     */

    public ArrayList<String> getArguments() {
        return arguments;
    }
}
