package org.notaris;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

class DataCreatorTest {

    @org.junit.jupiter.api.Test
    void create100RandomKeys() {
        DateFormat df = new SimpleDateFormat("mm:ss:SSS");

        long startTime = System.currentTimeMillis();
        String[] args = {"-n", "5000", "-m", "5", "-d", "3", "-l" , "4", "-k", "C:\\Users\\Notaris\\Desktop\\keyFile.txt"};
        DataCreator.main(args);
        long endTime = System.currentTimeMillis();
        System.out.println("Time to execute: " + df.format(endTime - startTime));
    }
}