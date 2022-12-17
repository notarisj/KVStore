package org.notaris.tree.trie;

import org.notaris.tree.Tree;

import java.util.HashMap;

public class Trie extends Tree<TrieNode> {

    public Trie(TrieNode root) {
        super(root);
    }

    public void insert(String word) {
        TrieNode current = getRoot();

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

    public void insert(String word, Object value) {
        TrieNode current = getRoot();

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

    public TrieNode find(String word) {
        TrieNode current = getRoot();
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
    }

}
