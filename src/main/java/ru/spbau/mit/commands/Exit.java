package ru.spbau.mit.commands;

import ru.spbau.mit.Shell;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
* The {@code Exit} class implements the command that exits the Shell.
*/

public class Exit implements Command {
    public Exit(ArrayList<String> args) {}

    /**
     * Sets {@code static Shell.exitRequest} variable to true, which is later checked by Shell to properly exit.
     */

    @Override
    public OutputStream run(InputStream in) {
        Shell.setExitRequest();
        return null;
    }
}
