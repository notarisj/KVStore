package org.notaris.key;

import lombok.Getter;
import lombok.Setter;
import org.notaris.tree.TreeNode;

import java.util.HashMap;

@Getter
@Setter
public class KeyNode extends TreeNode<String, KeyNode> {

    //private String name;
    private String value;
    private Integer type;
    public KeyNode() {
    }

    public KeyNode(HashMap<String, KeyNode> children) {
        setChildren(children);
    }
}
