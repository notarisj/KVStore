package org.notaris;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.Set;
import java.util.UUID;

public class DataCreationUtils {

    public static void createValue(JSONObject node, Integer nesting, Integer maxKeys, Integer maxNesting,
                                     Integer maxStrLength, Set<String> keyFile, Integer numberOfChildren) {
        nesting++;
        if (maxNesting <= nesting && nesting != 1) return;
        for (int i = 0; i < numberOfChildren; i++) {
            Object[] key = getRandomKey(keyFile, maxStrLength);
            JSONObject newNode = new JSONObject();
            int newNumberOfChildren = RandomUtils.nextInt(0, maxKeys + 1);
            if (newNumberOfChildren == 0 || nesting == maxNesting - 1) {
                node.put(key[0].toString(), key[2].toString());
            } else {
                node.put(key[0].toString(), newNode);
                createValue(newNode, nesting, maxKeys, maxNesting, maxStrLength, keyFile, newNumberOfChildren);
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

    public static String convertFromJSON(JSONObject json) {
        return json.toString()
                .replace(":", " -> ")
                .replace("{", "[")
                .replace("}", "]")
                .replace(",", " | ");
    }

}
