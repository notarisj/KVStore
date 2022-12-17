package org.notaris;

class DataCreatorTest {

    @org.junit.jupiter.api.Test
    void create100RandomKeys() {
        String[] args = {"-n", "100000", "-m", "5", "-d", "5", "-l" , "50", "-k", "C:\\Users\\Notaris\\Desktop\\keyFile.txt"};
        DataCreator dc = new DataCreator();
        dc.main(args);
    }
}