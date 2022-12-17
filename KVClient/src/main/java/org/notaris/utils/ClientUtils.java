package org.notaris.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Scanner;

public class ClientUtils {
    public static String readUserInput(BufferedReader in, String ip) throws IOException {
        String userData = "";
        Scanner input = new Scanner(in);
        while (true) {
            String line = input.nextLine();
            userData += line;
            if (line.contains(";")) {
                break;
            }
            System.out.print("kvserver@" + ip + "> ");
        }
        return userData;
    }
}
