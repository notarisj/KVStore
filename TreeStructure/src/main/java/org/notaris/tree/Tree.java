package org.notaris.tree;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Tree<Node> {
    private Node root;

    public Tree(Node root) {
        this.root = root;
    }
}
