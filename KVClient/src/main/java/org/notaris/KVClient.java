package org.notaris;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.notaris.utils.ServerStruct;


public class KVClient {

    private static final Logger logger = LogManager.getLogger(KVClient.class);

    public static void main(String[] args1) throws IOException {
        // Create a Scanner to read input from the command line
        Scanner scanner = new Scanner(System.in);
        DateFormat df = new SimpleDateFormat("mm:ss:SSS");

        String[] args = {"-s", "C:\\Users\\Notaris\\Desktop\\serverFile.txt", "-i", "C:\\Users\\Notaris\\Desktop\\dataToIndex.txt",  "-k",  "2"};

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
        List<ServerStruct> servers = new ArrayList<>();

        // Loop over the list of server addresses and ports
        for (String server : serverFile) {
            String serverAddress = server.split(" ")[0];
            Integer port = Integer.valueOf(server.split(" ")[1]);

            // Create a new Socket to connect to the server on the specified port
            logger.info("Trying to connect to KVServer (" + serverAddress + ":" + port + ")...");
            Socket socket = new Socket(serverAddress, port);
            logger.info("Connected to server successfully.");

            // Add the socket to the list of sockets
            servers.add(new ServerStruct(socket));
        }

        // 2. Index Data
        long startTime = System.currentTimeMillis();
        boolean indexOk = true;
        int i = 1;
        for (String key : dataToIndex) {
            Collections.shuffle(servers);
            List<ServerStruct> randomSockets = servers.subList(0, replicationFactor);
            //logger.info("Key to be inserted: " + key);
            for (ServerStruct server : randomSockets) {

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
            Collections.shuffle(servers);


            // Prompt the user to enter a message to send to the server
            System.out.print("kvserver# ");
            String userCommand = scanner.nextLine();
            if (StringUtils.equals(userCommand, "")) continue;


            String[] command = UserCommandUtils.readCommand(userCommand);
            String commandType = command[0];


            String response = null;

            switch (commandType) {
                case "PUT" -> {
                    List<ServerStruct> socketsToSend = servers.subList(0, replicationFactor);
                    response = execute(socketsToSend, userCommand, commandType);
                }
                case "GET", "COMPUTE", "QUERY" -> { // Searches only in top level keys
                    List<ServerStruct> socketsToSend = servers.subList(0, replicationFactor + 1);
                    response = execute(socketsToSend, userCommand, commandType);
                }
                case "DELETE" -> {
                    if (allServersConnected(servers)) {
                        response = execute(servers, userCommand, commandType);
                    } else {
                        response = "SOME SERVERS ARE OFFLINE. CANNOT PERFORM DELETE.";
                    }
                }
            }
            System.out.println(response);


        }
    }

    private static String execute(List<ServerStruct> servers, String userCommand, String commandType) throws IOException {
        StringBuilder sb = new StringBuilder();
        String latestResponse = null;
        for (ServerStruct server : servers) {
            if (server.isConnected) {
                // Send the message to the server
                //server.out.println(message);
                server.out.write(userCommand + "\n");
                server.out.flush();

                // Check if the user entered "exit" to quit the program
                if (userCommand.equalsIgnoreCase("exit")) {
                    break;
                }

                String response = getStringFromBufferedReader(server.in);

                if (!StringUtils.equals(response, "BAD SERVER")) {
                    if (StringUtils.equals(response.substring(0, 1), "0")) { // ALL OK
                        if (StringUtils.equals(commandType, "GET") ||
                                StringUtils.equals(commandType, "COMPUTE") ||
                                StringUtils.equals(commandType, "QUERY")) {
                            return new StringBuilder()
                                    .append("\n")
                                    .append(response.substring(2)) // remove code (0 or 1) from response
                                    .append("\n")
                                    .toString();
                        } else {
                            sb.append("\n")
                                    .append("Server: ")
                                    .append(server.socket.getInetAddress().toString().replace("/", ""))
                                    .append(":")
                                    .append(server.socket.getLocalPort())
                                    .append("\n")
                                    .append(response.substring(2)) // remove code (0 or 1) from response
                                    .append("\n");
                        }
                    } else if (StringUtils.equals(response.substring(0, 1), "1")) {
                        latestResponse = new StringBuilder()
                                .append("\n")
                                .append(response.substring(2)) // remove code (0 or 1) from response
                                .append("\n")
                                .toString();
                    }
                } else {
                    server.isConnected = false;
                    server.socket.close();
                    server.in.close();
                    server.out.close();
                }
            }
        }
        if (sb.isEmpty()) {
            return latestResponse;
        } else {
            return sb.toString();
        }
    }

    private static Boolean allServersConnected(List<ServerStruct> servers) {
        boolean allConnected = true;
        for (ServerStruct server : servers) {
            if (!server.isConnected) {
                allConnected = false;
                break;
            }
        }
        return allConnected;
    }

    public static String getStringFromBufferedReader(BufferedReader in) throws IOException {
        StringBuilder sb = new StringBuilder();
        try {
            String line = in.readLine();
            while (!StringUtils.equals(line, "END")) {
                sb.append(line).append("\n");
                line = in.readLine();
            }
        } catch (SocketException e) {
            return "BAD SERVER";
        }
        if (sb.isEmpty()) {
            return sb.toString();
        } else {
            return sb.toString().substring(0,sb.length() - 2);
        }

    }
}