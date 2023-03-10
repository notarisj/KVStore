import org.junit.Test;
import org.notaris.DataCreation;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class DataCreationTest {

    @Test
    public void generateRandom1000Keys() {
        DateFormat df = new SimpleDateFormat("mm:ss:SSS");

        long startTime = System.currentTimeMillis();
        String[] args = {"-n", "1000", "-m", "5", "-d", "3", "-l" , "4", "-k", System.getProperty("user.home") + "/Desktop/keyFile.txt"};
        DataCreation.main(args);
        long endTime = System.currentTimeMillis();
        System.out.println("Time to execute: " + df.format(endTime - startTime));
    }
}