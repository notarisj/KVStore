package org.notaris;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.Set;
import java.util.UUID;

public class DataCreationUtils {

    /**
     * Generates a random value for the KVStore with the given parameters.
     * @param key The JSONObject used to represent the key.
     * @param nesting It is used to know the nesting level when using recursion, so we don't exceed the maximum nesting.
     * @param maxKeys The maximum number of keys (attributes) inside each value.
     * @param maxNesting The maximum level of nesting a key is allowed.
     * @param maxStrLength The maximum length of a string value.
     * @param keyFile File with attribute names.
     * @param numberOfChildren We must know the number of children of the newKey because if it's zero we must assign it a value.
     */
    public static void createRandomKey(JSONObject key, Integer nesting, Integer maxKeys, Integer maxNesting,
                                       Integer maxStrLength, Set<String> keyFile, Integer numberOfChildren) {
        nesting++;
        if (maxNesting < nesting) return;
        for (int i = 0; i < numberOfChildren; i++) {
            Object[] randAttr = getRandomAttribute(keyFile, maxStrLength);
            JSONObject newKey = new JSONObject();
            int newNumberOfChildren = RandomUtils.nextInt(0, maxKeys + 1);
            if (newNumberOfChildren == 0 || nesting.equals(maxNesting)) {
                key.put(randAttr[0].toString(), randAttr[2].toString());
            } else {
                key.put(randAttr[0].toString(), newKey);
                createRandomKey(newKey, nesting, maxKeys, maxNesting, maxStrLength, keyFile, newNumberOfChildren);
            }
        }
    }

    protected static Object[] getRandomAttribute(Set<String> keyFile, Integer maxStrLength) {
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
        return new Object[] {uuid, RandomUtils.nextInt(1, 4), getRandomValue(maxStrLength)};
    }

    private static Object getRandomValue(Integer maxStrLength) {
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
