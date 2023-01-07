package org.notaris;

public class SCUtils {

    /**
     * Extracts the key name from the PUT user command
     * @param key The input is the format: "keyName" -> ... or keyName -> ...
     * @return The name of the key (e.g. keyName)
     */
    public static String getKeyName(String key) {
        return key.substring(0, key.indexOf(" -> ")).replace("\"", "");
    }
}
