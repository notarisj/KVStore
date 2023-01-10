package org.notaris;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.notaris.tree.trie.Trie;
import org.notaris.tree.trie.TrieNode;
import org.notaris.tree.trie.TrieUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

public class ClientHandlerUtils {

    protected static String handlePut(String rightPart, Trie mainDB) {
        if (saveKey(rightPart, mainDB) != null) {
            return SCConstants.RESPONSE_OK + "\nKEY INSERTED";
        } else {
            return SCConstants.RESPONSE_BAD + "\nTHERE WAS AN ERROR IN THE FORMAT OF THE KEY.";
        }
    }

    protected static String handleGet(String rightPart, Trie mainDB) {
        if (StringUtils.equals(rightPart, "*")) {
            StringBuilder builder = new StringBuilder();
            HashMap<String, TrieNode> children = new HashMap<>();
            TrieUtils.findChildren(mainDB.getRoot(), builder, children, true);
            StringBuilder sb = new StringBuilder();
            for (String key : children.keySet()) {
                sb.append(key).append("\n");
            }
            String keys = sb.toString();
            return SCConstants.RESPONSE_OK + "\n" + keys;
        }
        TrieNode keyValue = TrieUtils.find(rightPart, mainDB);
        if (keyValue != null) {
            Trie keyToFind = (Trie) keyValue.getValue();
            return SCConstants.RESPONSE_OK + "\n" + rightPart + " -> " + TrieUtils.convertTrieToPrettyString(keyToFind);
        } else {
            return SCConstants.RESPONSE_WARN + "\nKEY WAS NOT FOUND";
        }
    }

    protected static String handleQuery(String rightPart, Trie mainDB) {
        Object key = findKey(rightPart, mainDB);
        if (key instanceof Trie) {
            return SCConstants.RESPONSE_OK + "\n" + rightPart + " -> " + TrieUtils.convertTrieToPrettyString((Trie) key);
        } else if (key instanceof String) {
            return SCConstants.RESPONSE_OK + "\n" + rightPart + " -> " + key;
        } else {
            return SCConstants.RESPONSE_WARN + "\nKEY WAS NOT FOUND";
        }
    }

    protected static String handleDelete(String rightPart, Trie mainDB) {
        boolean keyDeleted = TrieUtils.delete(rightPart, mainDB);
        if (keyDeleted) {
            return SCConstants.RESPONSE_OK + "\nKEY DELETED SUCCESSFULLY";
        } else {
            return SCConstants.RESPONSE_WARN + "\nKEY DOES NOT EXISTS OR IS NOT A TOP LEVEL KEY: " + rightPart;
        }
    }

    private static String convertToJSON(String key) {
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

    private static Trie saveKey(String key, Trie mainDB) {
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

    private static Object findKey(String key, Trie mainDB) {
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

    /**
     * Saves the key to the database.
     * @param jsonObject A JSONObject representing the key to be inserted.
     * @param mainDB A Trie instance of the main db.
     */
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
