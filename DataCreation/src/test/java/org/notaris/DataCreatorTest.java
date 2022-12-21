package org.notaris;

class DataCreatorTest {

    @org.junit.jupiter.api.Test
    void create100RandomKeys() {
        String[] args = {"-n", "10000", "-m", "5", "-d", "5", "-l" , "50", "-k", "/Users/notaris/Desktop/keyFile.txt"};
        DataCreator dc = new DataCreator();
        dc.main(args);
    }
}