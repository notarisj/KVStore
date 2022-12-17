package org.notaris;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IO {

    /**
     * Returns a String list with every line of the text file.
     *
     * @param path The path of the input file.
     * @return List of strings with every file line.
     */
    public static Set<String> readFile(String path) {
        try {
            FileReader reader = new FileReader(path);
            BufferedReader bufferedReader = new BufferedReader(reader);

            Set<String> inputFile = new HashSet<>();

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                inputFile.add(line);
            }
            reader.close();

            return inputFile;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Creates a new text file with the given path and lines.
     *
     * @param path      The path to create the file or file name for the default directory.
     * @param writeFile List of strings with every file line.
     */
    public static boolean writeFile(String path, List<String> writeFile) {
        try {
            FileWriter writer = new FileWriter(path, false);

            for (String line : writeFile) {
                writer.write(line);
                writer.write("\r\n");   // write new line
            }
            writer.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}