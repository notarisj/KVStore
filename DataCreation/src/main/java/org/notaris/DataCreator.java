package org.notaris;

import org.apache.commons.lang3.StringUtils;
import org.notaris.key.Key;
import org.notaris.key.KeyNode;
import org.notaris.key.KeyUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class DataCreator {

    static Integer size;            // indicates the number of lines
    static Integer maxNesting;      // is the maximum level of nesting
    static Integer maxKeys;         // is the maximum number of keys inside each value
    static Integer maxStrLength;    // is the maximum length of a string value
    static String keyFilePath;      // keyFile path
    static Set<String> keyFile;

    private static final Logger logger = LogManager.getLogger(DataCreator.class);

    public static void main(String[] args) {

        //String[] args = {"-n", "100", "-m", "4", "-d", "4", "-l" , "50", "-k", "C:\\Users\\Notaris\\Desktop\\keyFile.txt"};

        if (args.length != 10) {
            logger.error("Missing parameters!");
            throw new RuntimeException("Please provide all parameters.");
        }

        System.out.println("*---------------------------------------------------------------------------------*");
        System.out.println("*                          KVStore (v1.0): Data creation                          *");
        System.out.println("*---------------------------------------------------------------------------------*");

        logger.info("Trying to read parameters...");

        for (int i = 0; i <= 9; i = i + 2) {
            String param = args[i];
            if (StringUtils.equals(param, "-n")) {
                size = Integer.valueOf(args[i + 1]);
                logger.info("Size parameter read successfully.");
            } else if (StringUtils.equals(param, "-d")) {
                maxNesting = Integer.valueOf(args[i + 1]);
                logger.info("Max nesting parameter read successfully.");
            } else if (StringUtils.equals(param, "-m")) {
                maxKeys = Integer.valueOf(args[i + 1]);
                logger.info("Max keys parameter read successfully.");
            } else if (StringUtils.equals(param, "-l")) {
                maxStrLength = Integer.valueOf(args[i + 1]);
                logger.info("Max string length parameter read successfully.");
            } else if (StringUtils.equals(param, "-k")) {
                keyFilePath = args[i + 1];
                logger.info("Key file path parameter read successfully.");
            }
        }

        logger.info("Trying to generate output file with " + size + " keys.");

        List<String> outputFile = new ArrayList<>();

        for (int i = 1; i <= size; i = i + 1) {
            keyFile = IO.readFile(keyFilePath);
            Key key = new Key(new KeyNode());
            Integer nesting = 0;
            KeyUtils.createValue(key.getRoot(), nesting, maxKeys, maxNesting, maxStrLength, keyFile);
            KeyUtils.cleanUpParentValues(key.getRoot());
            //System.out.println(KeyUtils.cleanUpJSON(KeyUtils.convertToJSON(key).replace("\"root\"", "\"key" + i + "\"")));
            String _key = KeyUtils.cleanUpJSON(KeyUtils.convertToJSON(key).replace("\"root\"", "\"key" + i + "\"")).replace("\"root\"", "\"key" + i +"\"");
            outputFile.add(_key);
            logger.info(_key);
        }
        logger.info("All keys generated successfully!");
        logger.info("Trying to write output file...");
        IO.writeFile("/Users/notaris/Desktop//outputFile.txt", outputFile);
        logger.info("Output file generated successfully!");
    }
}