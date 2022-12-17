package org.notaris.key;

import lombok.Getter;
import lombok.Setter;

import org.notaris.tree.Tree;

@Getter
@Setter
public class Key extends Tree<KeyNode> {

    public Key(KeyNode root) {
        super(root);
    }
}
