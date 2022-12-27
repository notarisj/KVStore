package org.notaris.tree.trie;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

public class TrieUtils {

    public static void insert(String word, Trie trie) {
        TrieNode current = trie.getRoot();

        int i = 1;
        char[] toBeInserted = word.toCharArray();
        for (char _char : toBeInserted) {
            /*
              If the following keys where to be added: "key" and "key2"; the node
              for the "y" character of "key" will have a null children. That will
              cause a NullPointer when "key2" tries to insert character "2".
             */
            //if (current.getEndOfWord()) current.setChildren(new HashMap<>());
            if (current.getEndOfWord() && current.getChildren() == null) {
                current.setChildren(new HashMap<>());
            }

            if (i == toBeInserted.length) { // is leaf node -> don't create children hashmap
                current = current.getChildren().computeIfAbsent(_char, c -> new TrieNode());
            } else { // create children hashmap
                current = current.getChildren().computeIfAbsent(_char, c -> new TrieNode(new HashMap<>()));
            }
            i++;
        }
        current.setEndOfWord(true);
    }

    public static void insert(String word, Object value, Trie trie) {
        TrieNode current = trie.getRoot();

        int i = 1;
        char[] toBeInserted = word.toCharArray();
        for (char _char : toBeInserted) {
            /*
              If the following keys where to be added: "key" and "key2"; the node
              for the "y" character of "key" will have a null children. That will
              cause a NullPointer when "key2" tries to insert character "2".
             */
            if (current.getEndOfWord()) current.setChildren(new HashMap<>());

            if (i == toBeInserted.length) { // is leaf node -> don't create children hashmap
                current = current.getChildren().computeIfAbsent(_char, c -> new TrieNode());
                current.setValue(value);
            } else { // create children hashmap
                current = current.getChildren().computeIfAbsent(_char, c -> new TrieNode(new HashMap<>()));
            }
            i++;
        }
        current.setEndOfWord(true);
    }

    /**
     * Searches the Trie for the word given. If the word exists it
     * will return the TrieNode of the last character of the word.
     * @param word String to search
     * @param trie Trie db
     * @return The TrieNode of the last character of the given word
     */
    public static TrieNode find(String word, Trie trie) {
        try {
            TrieNode current = trie.getRoot();
            for (int i = 0; i < word.length(); i++) {
                char ch = word.charAt(i);
                TrieNode node = current.getChildren().get(ch);
                if (node == null) {
                    return null;
                }
                current = node;
            }
            if (current.getEndOfWord()) {
                return current;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static Boolean delete(String key, Trie mainDB) {
        // find method will return != null only if the key is a top level key,
        // so we can delete it
        if (find(key, mainDB) != null) {
            delete(mainDB.getRoot(), key, 0);
            return true;
        } else {
            return false;
        }
    }

    private static boolean delete(TrieNode current, String key, int index) {
        if (index == key.length()) {
            if (!current.getEndOfWord()) {
                return false;
            }
            current.setEndOfWord(false);
            return current.getChildren() == null;
        }
        char ch = key.charAt(index);
        TrieNode node = current.getChildren().get(ch);
        if (node == null) {
            return false;
        }
        boolean shouldDeleteCurrentNode = delete(node, key, index + 1) && !node.getEndOfWord();

        if (shouldDeleteCurrentNode) {
            current.getChildren().remove(ch);
            return current.getChildren() == null;
        }
        return false;
    }

    public static String getKey(Trie trie) {
        if (trie != null) {
            JSONObject obj = new JSONObject();
            TrieUtils.createJSONObject(trie.getRoot(), obj);
            return obj.toString()
                    .replace(":", " -> ")
                    .replace(",", " | ")
                    .replace("{", "[")
                    .replace("}", "]");
        } else {
            return "[]";
        }
    }

    public static void createJSONObject(TrieNode trieNode, JSONObject finalKey) {
        StringBuilder builder = new StringBuilder();
        HashMap<String, TrieNode> children = new HashMap<>();
        findChildren(trieNode, builder, children, true);
        for (Map.Entry<String, TrieNode> child : children.entrySet()) {
            Object value = child.getValue().getValue();
            if (value instanceof Trie) {
                finalKey.put(child.getKey(), new JSONObject());
                createJSONObject(((Trie) value).getRoot(), (JSONObject) finalKey.get(child.getKey()));
            } else if (value instanceof String) {
                finalKey.put(child.getKey(), value);
            } else if (value == null) {
                finalKey.put(child.getKey(), new JSONObject());
            }
        }
    }

    public static void findChildren(TrieNode trieNode, StringBuilder currName, HashMap<String, TrieNode> finalChildren, boolean allowNest) {
        HashMap<Character, TrieNode> children = trieNode.getChildren();
        if (children != null) {
            for (Map.Entry<Character, TrieNode> child : children.entrySet()) {
                currName.append(child.getKey());
                if (child.getValue().getEndOfWord()) {
                    finalChildren.put(currName.toString(), child.getValue());
                    findChildren(child.getValue(), currName, finalChildren, false);
                } else {
                    findChildren(child.getValue(), currName, finalChildren, true);
                }
            }
        } else if (trieNode.getValue() instanceof Trie trie && allowNest) {
            findChildren(trie.getRoot(), currName, finalChildren, false);
        }
        if (currName.length() >= 1) currName.deleteCharAt(currName.length() - 1);
    }
}
