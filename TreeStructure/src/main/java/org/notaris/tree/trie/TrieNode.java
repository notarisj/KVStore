package org.notaris.tree.trie;

import lombok.Getter;
import lombok.Setter;
import org.notaris.tree.TreeNode;

import java.util.HashMap;

@Getter
@Setter
public class TrieNode extends TreeNode<Character, TrieNode> {
    private Object value;
    private Boolean endOfWord = false;

    public TrieNode() {
    }

    public TrieNode(HashMap<Character, TrieNode> children) {
        setChildren(children);
    }
}
