package org.notaris.tree;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Getter
@Setter
public class TreeNode<K, V> {
    private HashMap<K, V> children;
}
