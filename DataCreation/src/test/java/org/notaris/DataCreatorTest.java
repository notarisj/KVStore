package org.notaris;

class DataCreatorTest {

    @org.junit.jupiter.api.Test
    void create100RandomKeys() {
        String[] args = {"-n", "3000000", "-m", "5", "-d", "3", "-l" , "4", "-k", "C:\\Users\\Notaris\\Desktop\\keyFile.txt"};
        DataCreator.main(args);
    }
}