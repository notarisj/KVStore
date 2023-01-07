package org.notaris;

import org.json.JSONException;
import org.json.JSONObject;
import org.notaris.tree.trie.Trie;
import org.notaris.tree.trie.TrieNode;
import org.notaris.tree.trie.TrieUtils;

import java.util.*;

public class ServerUtils {

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

    public static Trie saveKey(String key, Trie mainDB) {
        try {
            JSONObject jsonObject = new JSONObject(convertToJSON(key));
            String keyName = SCUtils.getKeyName(key);
            TrieUtils.insert(keyName, mainDB);
            indexJSONObject(jsonObject, mainDB, keyName);
            // INSERT WAS OK
            return mainDB;
        } catch (JSONException e) {
            // THERE WAS AN ERROR IN THE FORMAT OF THE KEY
            return null;
        }
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

    private static void indexJSONObject(JSONObject jsonObject, Trie mainDB, String parentKeyName) {
        Iterator<String> keys = jsonObject.keys();
        Trie parentTrie = new Trie(new TrieNode(new HashMap<>()));
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = jsonObject.get(key);
            if (value instanceof JSONObject) {
                TrieUtils.insert(key, parentTrie);
                Objects.requireNonNull(TrieUtils.find(parentKeyName, mainDB)).setValue(parentTrie);
                indexJSONObject((JSONObject) value, parentTrie, key);
            } else {
                TrieUtils.insert(key, value, parentTrie);
                Objects.requireNonNull(TrieUtils.find(parentKeyName, mainDB)).setValue(parentTrie);
            }
        }
        if (jsonObject.isEmpty()) {
            Objects.requireNonNull(TrieUtils.find(parentKeyName, mainDB)).setValue(null);
        }
    }
}
