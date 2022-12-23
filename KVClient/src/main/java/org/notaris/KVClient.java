package org.notaris;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.notaris.utils.SocketStruct;


public class KVClient {

    private static final Logger logger = LogManager.getLogger(KVClient.class);

    public static void main(String[] args1) throws IOException {
        // Create a Scanner to read input from the command line
        Scanner scanner = new Scanner(System.in);
        DateFormat df = new SimpleDateFormat("mm:ss:SSS");

        String[] args = {"-s", "/Users/notaris/Desktop/serverFile.txt", "-i", "/Users/notaris/Desktop/dataToIndex.txt",  "-k",  "1"};

        Set<String> serverFile = null;
        Set<String> dataToIndex = null;
        int replicationFactor = 0;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-s")) {
                serverFile = IO.readFile(args[i+1]);
            }
            else if (args[i].equals("-i")) {
                dataToIndex = IO.readFile(args[i+1]);
            }
            else if (args[i].equals("-k")) {
                replicationFactor = Integer.valueOf(args[i+1]);
            }
        }

        // 1. Connect to all servers

        // Create a list to hold the sockets
        List<SocketStruct> sockets = new ArrayList<>();

        // Loop over the list of server addresses and ports
        for (String server : serverFile) {
            String serverAddress = server.split(" ")[0];
            Integer port = Integer.valueOf(server.split(" ")[1]);

            // Create a new Socket to connect to the server on the specified port
            logger.info("Trying to connect to KVServer (" + serverAddress + ":" + port + ")...");
            Socket socket = new Socket(serverAddress, port);
            logger.info("Connected to server successfully.");

            // Add the socket to the list of sockets
            sockets.add(new SocketStruct(socket));
        }

        // 2. Index Data
        long startTime = System.currentTimeMillis();
        boolean indexOk = true;
        int i = 1;
        for (String key : dataToIndex) {
            Collections.shuffle(sockets);
            List<SocketStruct> randomSockets = sockets.subList(0, replicationFactor);
            //logger.info("Key to be inserted: " + key);
            for (SocketStruct server : randomSockets) {

                System.out.println("BEFORE key index server: " + server.toString());

                server.out.write("PUT " + key + " \n");
                server.out.flush();
                String response = getStringFromBufferedReader(server.in);
                System.out.println("SERVER response: " + response);
                if (!response.contains("KEY INSERTED")) {
                    indexOk = false;
                    System.out.println(key);
                }
            }
            System.out.println("i = " + i);
            i++;
        }
        long endTime = System.currentTimeMillis();

        if (indexOk) {
            logger.info("Index finished successfully");
        } else {
            logger.info("Something went wrong while indexing");
        }
        logger.info("Time to execute: " + df.format(endTime - startTime));

        System.out.print("\n------------------ Welcome to KVClient ------------------\n");
        System.out.print("Enter a command to send to the server (or 'exit' to quit)\n\n");

        // Loop until the user enters "exit"
        while (true) {
            Collections.shuffle(sockets);
//            List<SocketStruct> randomSockets = sockets.subList(0, replicationFactor + 1);
            List<SocketStruct> randomSockets = sockets.subList(0, replicationFactor);

            // Prompt the user to enter a message to send to the server
            System.out.print("kvserver# ");
            String command = scanner.nextLine();
            if (StringUtils.equals(command, "")) continue;

            for (SocketStruct server : randomSockets) {
                // Send the message to the server
                //server.out.println(message);
                server.out.write(command + "\n");
                server.out.flush();

                // Check if the user entered "exit" to quit the program
                if (command.equalsIgnoreCase("exit")) {
                    break;
                }

                String[] commandArray = UserCommandUtils.readCommand(command);
                String commandType = commandArray[0];
                String rightPart = commandArray[1];

                String response = getStringFromBufferedReader(server.in);
                switch (commandType) {
                    case "QUERY":
                        if (response.contains(rightPart)) {
                            System.out.println(response);
                            break;
                        }
                        break;
                    case "DELETE", "PUT", "GET", "COMPUTE":
                        System.out.println(response);
                        break;
                }
            }
        }
    }

    public static String getStringFromBufferedReader(BufferedReader in) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line = in.readLine();
        while (!StringUtils.equals(line, "END")) {
            sb.append(line);
            line = in.readLine();
        }
        return sb.toString();
    }
}