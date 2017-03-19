package ru.spbau.mit.utils;

import java.util.HashMap;

/**
* The {@code Environment} class provides support for maintaining user-specified environment variables.
*/

public final class Environment {
    private static Environment environmentInstance = new Environment();
    private HashMap<String, String> environment = new HashMap<>();

    private Environment(){}

    /**
     * returns singleton instance of environments
     */

    public static Environment getInstance() {
        return environmentInstance;
    }

    /**
     * sets a value for a variable varName in environment
     */

    public void updateEnv(String varName, String value) {
        environment.put(varName, value);
    }

    /**
     * returns this varName's value from environment
     */

    public String getVarValue(String varName) {
        return environment.get(varName);
    }
}
