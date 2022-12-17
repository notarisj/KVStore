package org.notaris.tree.trie;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public class TrieUtils {

    public static String getKey(Trie trie) {
        JSONObject obj = new JSONObject();
        TrieUtils.createJSONObject(trie.getRoot(), obj);
        return obj.toString()
                .replace(":", " -> ")
                .replace(",", " | ")
                .replace("{", "[")
                .replace("}", "]");
    }

    public static void createJSONObject(TrieNode trieNode, JSONObject finalKey) {
        StringBuilder builder = new StringBuilder();
        HashMap<String, TrieNode> children = new HashMap<>();
        findChildren(trieNode, builder, children, true);
        if (children != null) {

            for (Map.Entry<String, TrieNode> child : children.entrySet()) {

                Object value = child.getValue().getValue();

                if (value instanceof Trie) {
                    finalKey.put(child.getKey(), new JSONObject());
                    createJSONObject(((Trie) value).getRoot(), (JSONObject) finalKey.get(child.getKey()));
                } else if (value instanceof String) {
                    finalKey.put(child.getKey(), value);
                }

            }

        }
    }

//    /**
//     * ChatGPT version
//     * @param trieNode
//     * @param finalKey
//     */
//    public static void createJSONObject(TrieNode trieNode, JSONObject finalKey) {
//        StringBuilder builder = new StringBuilder();
//        HashMap<String, TrieNode> children = new HashMap<>();
//        findChildren(trieNode, builder, children, true);
//        if (children != null) {
//            for (Map.Entry<String, TrieNode> child : children.entrySet()) {
//                Object value = child.getValue().getValue();
//                if (value instanceof Trie) {
//                    JSONObject newObject = new JSONObject();
//                    finalKey.put(child.getKey(), newObject);
//                    createJSONObject(((Trie) value).getRoot(), newObject);
//                } else if (value instanceof String) {
//                    finalKey.put(child.getKey(), value);
//                }
//            }
//        }
//    }


    public static void findChildren(TrieNode trieNode, StringBuilder currName, HashMap<String, TrieNode> finalChildren, boolean allowNest) {
        HashMap<Character, TrieNode> children = trieNode.getChildren();
        if (children != null) {
            for (Map.Entry<Character, TrieNode> child : children.entrySet()) {
                currName.append(child.getKey());
                if (child.getValue().getEndOfWord() == true) {
                    finalChildren.put(currName.toString(), child.getValue());
                    findChildren(child.getValue(), currName, finalChildren, false);
                } else {
                    findChildren(child.getValue(), currName, finalChildren, true);
                }
            }
        } else if (trieNode.getValue() instanceof Trie && allowNest == true) {
            Trie trie = (Trie) trieNode.getValue();
            findChildren(trie.getRoot(), currName, finalChildren, false);
        }
        if (currName.length() >= 1) currName.deleteCharAt(currName.length() - 1);
    }


//    /**
//     * ChatGPT version
//     * @param trieNode
//     * @param currName
//     * @param finalChildren
//     * @param allowNest
//     */
//    public static void findChildren(TrieNode trieNode, StringBuilder currName, HashMap<String, TrieNode> finalChildren, boolean allowNest) {
//        HashMap<Character, TrieNode> children = trieNode.getChildren();
//        if (children != null) {
//            for (Map.Entry<Character, TrieNode> child : children.entrySet()) {
//                currName.append(child.getKey());
//                if (child.getValue().getEndOfWord() == true) {
//                    finalChildren.put(currName.toString(), child.getValue());
//                    findChildren(child.getValue(), currName, finalChildren, false);
//                } else {
//                    findChildren(child.getValue(), currName, finalChildren, true);
//                }
//                currName.deleteCharAt(currName.length() - 1);
//            }
//        } else if (trieNode.getValue() instanceof Trie && allowNest == true) {
//            Trie trie = (Trie) trieNode.getValue();
//            findChildren(trie.getRoot(), currName, finalChildren, false);
//        }
//    }




}
