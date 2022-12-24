package org.notaris;

import org.json.JSONObject;
import org.notaris.tree.trie.Trie;
import org.notaris.tree.trie.TrieNode;
import org.notaris.tree.trie.TrieUtils;

import java.io.BufferedReader;
import java.util.*;

public class ServerUtils {

    public static void parseKey(String key) {
        String name = getKeyName(key);

    }

    protected static String convertToJSON(String key) {
        String json = key.substring(key.indexOf(" -> ") + 4)
                .replace(" -> ", ":")
                .replace(" | ", ",")
                .replace("[", "{")
                .replace("]", "}");
        if (!json.contains("{")) {
            json = "{" + json + ":\"\"}";
        }
        return json;
    }

    protected static String getKeyName(String key) {
        String keyName = key.substring(0, key.indexOf(" -> ")).replace("\"", "");
        return keyName;
    }

    public static Trie saveKey(String key, Trie mainDB) {
        JSONObject jsonObject = new JSONObject(convertToJSON(key));
        String keyName = getKeyName(key);
        TrieUtils.insert(keyName, mainDB);
        indexJSONObject(jsonObject, mainDB, keyName);

        return mainDB;
    }

    public static void traverse(String key, Trie mainDB) {

    }

    public static Object findKey(String key, Trie mainDB) {
        String[] keyParts = key.split("\\.");
        Trie currDB = mainDB;

        for (int i = 0; i < keyParts.length; i++) {
            TrieNode currNode = TrieUtils.find(keyParts[i], currDB);

            if (currNode != null){
                if (i == keyParts.length - 1 && currNode.getValue() != null) {
                    return currNode.getValue();
                }

                if (currNode.getValue() instanceof Trie) {
                    currDB = (Trie) currNode.getValue();
                }
            }
        }
        return null;
    }

    public static String getCommandFirstPart(String cmd) {
        String[] cmdParts = cmd.split(" ");
        if (cmdParts.length >= 1) return cmdParts[0];
        return null;
    }

    public static String getCommandSecondPart(String cmd) {
        return cmd.replace("PUT ", "")
                .replace("GET ", "");
    }

    private static void indexJSONObject(JSONObject jsonObject, Trie mainDB, String parentKeyName) {

        Iterator<String> keys = jsonObject.keys();
        Trie parentTrie = new Trie(new TrieNode(new HashMap<>()));

        while (keys.hasNext()) {

            String key = keys.next();
            Object value = jsonObject.get(key);

            if (value instanceof JSONObject) {
                TrieUtils.insert(key, parentTrie);
                TrieUtils.find(parentKeyName, mainDB).setValue(parentTrie);
                indexJSONObject((JSONObject) value, parentTrie, key);
            } else {
                TrieUtils.insert(key, value, parentTrie);
                TrieUtils.find(parentKeyName, mainDB).setValue(parentTrie);
            }
        }
    }
}
