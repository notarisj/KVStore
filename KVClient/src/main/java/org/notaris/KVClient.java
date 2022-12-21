package org.notaris;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
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

        String[] args = {"-s", "/Users/notaris/Desktop/serverFile.txt", "-i", "/Users/notaris/Desktop/dataToIndex.txt",  "-k",  "2"};

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
        boolean indexOk = true;
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
        }

        if (indexOk) {
            logger.info("Index finished successfully");
        } else {
            logger.info("Something went wrong while indexing");
        }

        System.out.print("\n------------------ Welcome to KVClient ------------------\n");
        System.out.print("Enter a command to send to the server (or 'exit' to quit)\n\n");

        // Loop until the user enters "exit"
        while (true) {
            Collections.shuffle(sockets);
            List<SocketStruct> randomSockets = sockets.subList(0, replicationFactor + 1);

            // Prompt the user to enter a message to send to the server
            System.out.print("kvserver# ");
            String message = scanner.nextLine();
            if (StringUtils.equals(message, "")) continue;

            for (SocketStruct server : randomSockets) {
                // Send the message to the server
                //server.out.println(message);
                server.out.write(message + "\n");
                server.out.flush();

                // Check if the user entered "exit" to quit the program
                if (message.equalsIgnoreCase("exit")) {
                    break;
                }

                String command = message.contains(" ") ? message.substring(0, message.indexOf(' ')).trim() : message.trim();
                String rightPart = message.contains(" ") ? message.substring(message.indexOf(' ') + 1).trim() : "";

                String response = getStringFromBufferedReader(server.in);
                if (StringUtils.equals(command, "GET") && response.contains(rightPart)) {
                    System.out.println(response);
                    break;
                }
            }
        }
    }

    public static String getStringFromBufferedReader(BufferedReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while (!reader.ready()) {
            System.out.println("Thread slept for 1 ms");
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        while (reader.ready()) {
            line = reader.readLine();
            sb.append(line).append("\n");
        }
        if (sb.length() > 1) sb.deleteCharAt(sb.length() - 1);
        if (sb.length() > 1) sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
}