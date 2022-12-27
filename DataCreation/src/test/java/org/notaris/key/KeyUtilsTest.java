package org.notaris.key;

import org.apache.commons.lang3.RandomUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.notaris.IO;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class KeyUtilsTest {

    @Test
    void createValueV2() {
        int maxKeys = 5;
        int maxNesting = 5;
        int maxStrLength = 5;



        Integer nesting = 0;
        Set<String> keyFile = IO.readFile("C:\\Users\\Notaris\\Desktop\\keyFile.txt");
        JSONObject key = new JSONObject();
        int num =RandomUtils.nextInt(0, maxKeys + 1);
        KeyUtils.createValueV2(key, nesting, maxKeys, maxNesting, maxStrLength, keyFile, num);
        System.out.println(KeyUtils.convertFromJSON(key));



    }
}