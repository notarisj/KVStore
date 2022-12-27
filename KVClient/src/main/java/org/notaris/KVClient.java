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

    public static void main(String[] args) throws IOException {
        // Create a Scanner to read input from the command line
        Scanner scanner = new Scanner(System.in);
        DateFormat df = new SimpleDateFormat("mm:ss:SSS");

        Set<String> serverFile = null;
        Set<String> dataToIndex = null;
        int replicationFactor = 0;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-s" -> serverFile = IO.readFile(args[i + 1]);
                case "-i" -> dataToIndex = IO.readFile(args[i + 1]);
                case "-k" -> replicationFactor = Integer.parseInt(args[i + 1]);
            }
        }

        if (serverFile != null && dataToIndex != null) {
            // 1. Connect to all servers
            // Create a list to hold the sockets
            List<ServerStruct> servers = new ArrayList<>();

            // Loop over the list of server addresses and ports
            for (String server : serverFile) {
                String serverAddress = server.split(" ")[0];
                int port = Integer.parseInt(server.split(" ")[1]);

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
                for (ServerStruct server : randomSockets) {
                    server.out.write("PUT " + key + " \n");
                    server.out.flush();
                    String response = getStringFromBufferedReader(server.in);
                    if (!response.contains("KEY INSERTED")) {
                        indexOk = false;
                    }
                }
                System.out.println("\033[32m" + (i * 100) / (dataToIndex.size()) + "%\033[0m");
                i++;
            }
            long endTime = System.currentTimeMillis();

            if (indexOk) {
                logger.info("Index finished successfully");
            } else {
                logger.info("Something went wrong while indexing");
            }
            logger.info("Time to execute: " + df.format(endTime - startTime));

            System.out.print("\n------------------ \033[32m\033[1mWelcome to KVClient\033[0m ------------------\n");
            System.out.print("Enter a command to send to the server (or '\033[1mexit\033[0m' to quit)\n\n");

            // Loop until the user enters "exit"
            while (true) {
                Collections.shuffle(servers);

                // Prompt the user to enter a message to send to the server
                System.out.print("\033[32mkvserver# \033[0m");
                String userCommand = scanner.nextLine();
                if (StringUtils.equals(userCommand, "")) {
                    continue;
                } else if (userCommand.equalsIgnoreCase("exit")) {
                    logger.info("Quiting KVStore...");
                    break;
                }

                String response = handleCommand(userCommand, servers, replicationFactor);
                System.out.println(response);
            }
        } else {
            logger.info("No arguments where provided. Quiting KVClient...");
        }
    }

    private static String handleCommand(String userCommand, List<ServerStruct> servers, int replicationFactor) throws IOException {

        if (StringUtils.equals(userCommand, "help")) {
            return """
                    ------------------------ AVAILABLE COMMANDS ------------------------\s

                    \033[32mGET\033[0m:      returns only top level keys (e.g. GET key1)
                    \033[32mQUERY\033[0m:    returns any key (e.g. QUERY key1.attr1.attr2)
                    \033[32mCOMPUTE\033[0m:  returns the result of the expression
                              (e.g.
                              COMPUTE 2/(x+3*(y+z)) WHERE x = QUERY person1.address.number AND
                                                          y = QUERY person2.address.number AND
                                                          z = QUERY person3.address.number)
                    \033[32mPUT\033[0m:      inserts the key (e.g. PUT person1 -> [ name -> John | age -> 22 ])
                    \033[32mDELETE\033[0m:   deletes the key (e.g. DELETE key1)
                    """;
        }

        String[] command = UserCommandUtils.readCommand(userCommand);
        String commandType = command[0].toUpperCase();

        switch (commandType) {
            case "PUT" -> {
                String keyName = getKeyName(userCommand.substring(4));
                if (checkIfKeyExists(keyName, servers, replicationFactor)) {
                    return "\nKEY ALREADY EXISTS. YOU MUSTS DELETE IT FIRST\n";
                }

                // continue if key was not found
                List<ServerStruct> socketsToSend = servers.subList(0, replicationFactor);
                return executeCommand(socketsToSend, userCommand);
            }
            case "GET", "COMPUTE", "QUERY" -> { // Searches only in top level keys
                List<ServerStruct> socketsToSend = servers.subList(0, replicationFactor + 1);
                return executeCommand(socketsToSend, userCommand);
            }
            case "DELETE" -> {
                if (allServersConnected(servers)) {
                    return executeCommand(servers, userCommand);
                } else {
                    return "SOME SERVERS ARE OFFLINE. CANNOT PERFORM DELETE.";
                }
            }
            default -> {
                return "UNKNOWN COMMAND";
            }
        }
    }

    protected static String getKeyName(String key) {
        return key.substring(0, key.indexOf(" -> ")).replace("\"", "");
    }

    private static Boolean checkIfKeyExists(String keyName, List<ServerStruct> servers, int replicationFactor) throws IOException {
        // Check if key already exists
        String response = executeCommand(servers.subList(0, replicationFactor + 1), "GET " + keyName);
        return response.contains(keyName);
    }

    private static String executeCommand(List<ServerStruct> servers, String userCommand) throws IOException {

        String[] command = UserCommandUtils.readCommand(userCommand);
        String commandType = command[0].toUpperCase();

        StringBuilder sb = new StringBuilder();
        String latestResponse = null;
        for (ServerStruct server : servers) {
            if (server.isConnected) {
                // Send the message to the server
                //server.out.println(message);
                server.out.write(userCommand + "\n");
                server.out.flush();

                String response = getStringFromBufferedReader(server.in);

                if (!StringUtils.equals(response, "BAD SERVER")) {
                    if (StringUtils.equals(response.substring(0, 1), SCConstants.RESPONSE_OK)) {
                        if (StringUtils.equals(commandType, "GET") ||
                                StringUtils.equals(commandType, "COMPUTE") ||
                                StringUtils.equals(commandType, "QUERY")) {
                            return response.substring(2); // remove code (0 or 1) from response

                        } else {
                            sb.append("\nServer: ")
                                    .append(server.socket.getInetAddress().toString().replace("/", ""))
                                    .append(":")
                                    .append(server.socket.getLocalPort())
                                    .append("\n")
                                    .append(response.substring(2)); // remove code from response

                        }
                    } else if (StringUtils.equals(response.substring(0, 1), SCConstants.RESPONSE_WARN)) {
                        latestResponse = response.substring(2); // remove code from response
                    } else if (StringUtils.equals(response.substring(0, 1), SCConstants.RESPONSE_BAD)) {
                        return response.substring(2); // remove code from response
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
            return sb.substring(0,sb.length() - 2);
        }

    }
}