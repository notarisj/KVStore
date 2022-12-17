package org.notaris.key;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.notaris.MyStringUtils;
import org.notaris.Type;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class KeyUtils {

    public static String cleanUpJSON(String json) {
        json = json.substring(1, json.length() - 1)
                .replace(KeyFormat.REMOVE_EMPTY_CHILDREN, "")
                .replace(KeyFormat.REMOVE_EMPTY_VALUE, "")
                .replace(KeyFormat.REMOVE_EMPTY_TYPE, "")
                .replaceAll(KeyFormat.REMOVE_TYPE, "");
        json = removeChildrenBrackets(json);
        json = removeValueBrackets(json);
        json = json.replace(KeyFormat.REMOVE_VALUE_LABEL, "")
                .replace(KeyFormat.REMOVE_CHILDREN, " -> ")
                .replace(":", " -> ")
                .replace("{", "[")
                .replace("}", "]")
                .replace(",", " | ");

        return json;
    }

    private static String removeChildrenBrackets(String str) {
        int childrenLocation = str.indexOf("{\"children\":");
        if (childrenLocation != -1) { // children exists in str

//            if (StringUtils.equals("root", str.substring(childrenLocation - 6, childrenLocation - 2))) {
//                str = removeChildrenBrackets(str);
//            } else {
                str = removeChildrenBrackets(removeBrackets(str, childrenLocation));
//            }
        }
        return str;
    }

    private static String removeValueBrackets(String str) {
        int valueLocation = str.indexOf("{,\"value\":");
        if (valueLocation != -1) { // children exists in str
            str = removeValueBrackets(removeBrackets(str, valueLocation));
        }
        return str;
    }

    public static String removeBrackets(String str, Integer openingBracket) {
        StringBuilder sb = new StringBuilder(str);
        Integer closingBracket = MyStringUtils.findClosingBracket(str, openingBracket, '{');
        sb.deleteCharAt(closingBracket);
        sb.deleteCharAt(openingBracket);
        return sb.toString();
    }

    public static String convertToJSON(Key key) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .writer()
                .withDefaultPrettyPrinter();

        String json;
        try {
            json = mapper.writeValueAsString(key);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return json;
    }

    public static void createValue(KeyNode node, Integer nesting, Integer maxKeys, Integer maxNesting,
                                      Integer maxStrLength, Set<String> keyFile) {

        int numberOfChildren = RandomUtils.nextInt(0, maxKeys + 1);
        Object[] key = getRandomKey(keyFile, maxStrLength);
        nesting++;

        if (maxNesting <= nesting && nesting != 1) return;

        if (node.getChildren() == null) { // is root

            node.setChildren(new HashMap<>());
            node.setType(Integer.valueOf(key[1].toString()));
            node.setValue(key[2].toString());

            for (int i = 0; i < numberOfChildren; i++) {
                createValue(node, nesting, maxKeys, maxNesting, maxStrLength, keyFile);
            }
        } else {

            KeyNode keyNode = new KeyNode(new HashMap<>());

            keyNode.setType(Integer.valueOf(key[1].toString()));
            keyNode.setValue(key[2].toString());

            node.getChildren().put(key[0].toString(), keyNode);

            for (int i = 0; i < numberOfChildren; i++) {
                createValue(keyNode, nesting, maxKeys, maxNesting, maxStrLength, keyFile);
            }
        }
    }


    public static void cleanUpParentValues(KeyNode root) {
        if (root.getChildren() != null) {
            if (!root.getChildren().isEmpty()) {
                for (Map.Entry<String, KeyNode> child : root.getChildren().entrySet()) {
                    cleanUpParentValues(child.getValue());
                }
            }

            if (!root.getChildren().isEmpty()) {
                root.setType(null);
                root.setValue(null);
            }
        }
    }


    private static Object[] getRandomKey(Set<String> keyFile, Integer maxStrLength) {
        if (!keyFile.isEmpty()) {
            int item = RandomUtils.nextInt(1, keyFile.size());
            int i = 1;

            for (String keyName : keyFile) {
                String[] _keyName = keyName.split(" ");
                if (i == item) {
                    if (StringUtils.equals(_keyName[1], "int")) {
                        keyFile.remove(keyName);
                        return new Object[] {_keyName[0], 1, getValue(Type.TYPE_INTEGER, maxStrLength)};

                    } else if (StringUtils.equals(_keyName[1], "float")) {
                        keyFile.remove(keyName);
                        return new Object[] {_keyName[0], 2, getValue(Type.TYPE_FLOAT, maxStrLength)};

                    } else if (StringUtils.equals(_keyName[1], "string")) {
                        keyFile.remove(keyName);
                        return new Object[] {_keyName[0], 3, getValue(Type.TYPE_STRING, maxStrLength)};
                    }
                }
                i++;
            }
        }

        String uuid = UUID.randomUUID().toString();
        return new Object[] {uuid, RandomUtils.nextInt(1, 4), getValue(maxStrLength)};
    }


    private static Object getValue(Integer maxStrLength) {
        return getValue(RandomUtils.nextInt(1, 4), maxStrLength);
    }

    private static Object getValue(int type, Integer maxStrLength) {
        if (type == Type.TYPE_INTEGER) {
            return RandomUtils.nextInt();
        } else if (type == Type.TYPE_FLOAT) {
            return RandomUtils.nextFloat();
        } else if (type == Type.TYPE_STRING) {
            return RandomStringUtils.random(RandomUtils.nextInt(1, maxStrLength + 1), true, true);
        }
        return null;
    }

}
