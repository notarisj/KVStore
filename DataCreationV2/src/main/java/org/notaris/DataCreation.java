package org.notaris;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DataCreation {

    static Integer size;            // indicates the number of lines
    static Integer maxNesting;      // is the maximum level of nesting
    static Integer maxKeys;         // is the maximum number of keys inside each value
    static Integer maxStrLength;    // is the maximum length of a string value
    static String keyFilePath;      // keyFile path
    static Set<String> keyFile;

    private static final Logger logger = LogManager.getLogger(DataCreation.class);

    public static void main(String[] args) {

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-n" -> size = Integer.valueOf(args[i + 1]);
                case "-d" -> maxNesting = Integer.valueOf(args[i + 1]);
                case "-m" -> maxKeys = Integer.valueOf(args[i + 1]);
                case "-l" -> maxStrLength = Integer.valueOf(args[i + 1]);
                case "-k" -> keyFilePath = args[i + 1];
            }
        }

        logger.info("Trying to generate output file with " + size + " keys.");
        List<String> outputFile = new ArrayList<>();

        for (int i = 1; i <= size; i = i + 1) {
            Integer nesting = 0;
            keyFile = IO.readFile(keyFilePath);
            JSONObject key = new JSONObject();
            int num = RandomUtils.nextInt(0, maxKeys + 1);
            DataCreationUtils.createValue(key, nesting, maxKeys, maxNesting, maxStrLength, keyFile, num);

            String _key = "key" + i + " -> " + DataCreationUtils.convertFromJSON(key);
            outputFile.add(_key);
            logger.info(_key);
        }
        logger.info("All keys generated successfully!");
        logger.info("Trying to write output file...");
        IO.writeFile("/Users/notaris/Desktop//outputFile.txt", outputFile);
        logger.info("Output file generated successfully!");


    }

}
