package org.notaris;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientUtils {

    private static final Logger logger = LogManager.getLogger(ClientUtils.class);

    /**
     * Connects to all the servers.
     *
     * @param serverFile The server IPs and ports given by the user.
     * @return List of custom server struct.
     */
    protected static List<ServerStruct> connectToServers(Set<String> serverFile) throws IOException {
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
        return servers;
    }

    /**
     * Indexes the data.
     *
     * @param dataToIndex Set of string with data to index.
     * @param servers     The servers where the command will be executed.
     * @return True if index completed without errors and false otherwise.
     */
    protected static boolean indexData(Set<String> dataToIndex, List<ServerStruct> servers, int replicationFactor) throws IOException {
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
        return indexOk;
    }

    /**
     * Handles the command of the users and sends it to the servers accordingly.
     *
     * @param userCommand The command of the user.
     * @param servers     The servers where the command will be executed.
     * @return The response of the server.
     */
    protected static String handleCommand(String userCommand, List<ServerStruct> servers, int replicationFactor) throws IOException {
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

                Matcher matcher;
                Pattern pattern;

                pattern = Pattern.compile("(?<!\\s)->(?!\\s)");
                matcher = pattern.matcher(userCommand);
                userCommand = matcher.replaceAll(" -> ");

                pattern = Pattern.compile("\\|");
                matcher = pattern.matcher(userCommand);
                userCommand = matcher.replaceAll(" | ");

                System.out.println(userCommand);

                String keyName = SCUtils.getKeyName(userCommand.substring(4));
                if (checkIfKeyExists(keyName, servers, replicationFactor)) {
                    return "KEY ALREADY EXISTS. YOU MUSTS DELETE IT FIRST";
                }

                // continue if key was not found
                List<ServerStruct> socketsToSend = servers.subList(0, replicationFactor);
                return executeCommand(socketsToSend, userCommand, replicationFactor);
            }
            case "GET", "COMPUTE", "QUERY" -> { // Searches only in top level keys
                //List<ServerStruct> socketsToSend = servers.subList(0, servers.size() + 1 - replicationFactor);
                return executeCommand(servers, userCommand, replicationFactor);
            }
            case "DELETE" -> {
                if (allServersConnected(servers)) {
                    return executeCommand(servers, userCommand, replicationFactor);
                } else {
                    return "SOME SERVERS ARE OFFLINE. CANNOT PERFORM DELETE.";
                }
            }
            default -> {
                return "UNKNOWN COMMAND";
            }
        }
    }

    /**
     * Finds if a top-level key exists in the KVStore.
     *
     * @param keyName The name of the top-level key
     * @param servers The servers where the command will be executed.
     * @return True if key exists and false otherwise.
     */
    private static Boolean checkIfKeyExists(String keyName, List<ServerStruct> servers, int replicationFactor) throws IOException {
        // Check if key already exists
        String response = executeCommand(servers.subList(0, servers.size() + 1 - replicationFactor), "GET " + keyName, replicationFactor);
        return response.contains(keyName);
    }

    /**
     * Executes the command in the given servers.
     *
     * @param servers     The servers where the command will be executed.
     * @param userCommand The command of the user.
     * @return The response of the server.
     */
    private static String executeCommand(List<ServerStruct> servers, String userCommand, int replicationFactor) throws IOException {

        String[] command = UserCommandUtils.readCommand(userCommand);
        String commandType = command[0].toUpperCase();

        if (StringUtils.equals(commandType, "COMPUTE")) {
            return handleCompute(command[1], servers, replicationFactor);
        }

        StringBuilder sb = new StringBuilder();
        String latestResponse = null;
        for (ServerStruct server : servers) {
            if (server.isConnected) {
                // Send the message to the server
                //server.out.println(message);
                server.out.write(userCommand + "\n");
                server.out.flush();

                String response = getStringFromBufferedReader(server.in);

                if (!StringUtils.equals(response, "") && !StringUtils.equals(response, "BAD SERVER")) {
                    if (StringUtils.equals(response.substring(0, 1), SCConstants.RESPONSE_OK)) {
                        if (StringUtils.equals(commandType, "GET") ||
                                StringUtils.equals(commandType, "QUERY")) {
                            return response.substring(2);
                        } else {
                            sb.append("Server: ")
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

    protected static String handleCompute(String rightPart, List<ServerStruct> servers, int replicationFactor) {

        rightPart = handleCase(rightPart, "where", "WHERE");
        rightPart = handleCase(rightPart, "query", "QUERY");
        rightPart = handleCase(rightPart, "and", "AND");

        try {
            String strExpression = rightPart.substring(0, rightPart.indexOf("WHERE ") - 1);
            String[] queryParameters = rightPart.substring(rightPart.indexOf("WHERE ") + 6).split("AND");
            HashMap<String, Double> parameters = new HashMap<>();

            for (String parameter : queryParameters) {
                String[] queryArray = parameter.split("=");
                String variable = queryArray[0].trim();
                String query = queryArray[1].replace("QUERY ", "").trim();
                String resolvedValue = executeCommand(servers.subList(0, servers.size() + 1 - replicationFactor), "QUERY " + query, replicationFactor);
                String parsedResolvedValue = resolvedValue.substring(resolvedValue.indexOf(" -> ") + 4).split("\n")[0];
                parameters.put(variable, Double.valueOf(parsedResolvedValue));
            }

            Expression expression = new ExpressionBuilder(strExpression)
                    .variables(parameters.keySet())
                    .build();

            for (Map.Entry<String, Double> entry : parameters.entrySet()) {
                expression.setVariable(entry.getKey(), entry.getValue());
            }

            return String.valueOf(expression.evaluate());
        } catch (NumberFormatException e3) { // Double.valueOf(parsedResolvedValue)
            return "PROVIDED VARIABLES ARE NOT NUMERIC";
        } catch (IllegalArgumentException e1) { // new ExpressionBuilder(strExpression)
            return "MATH EXPRESSION CANNOT BE EMPTY";
        } catch (IndexOutOfBoundsException e2) { // substrings
            return "THERE WAS AN ERROR IN THE SYNTAX OF THE QUERY";
        } catch (IOException e) {
            return "THERE WAS AN ERROR WHILE READING THE COMMAND";
        }
    }

    private static String handleCase(String str, String search, String replacement) {
        Pattern pattern = Pattern.compile(search);
        Matcher matcher = pattern.matcher(str);
        return matcher.replaceAll(replacement);
    }

    /**
     * Checks if all declared servers are still connected.
     *
     * @param servers The list of the custom server struct
     * @return True if all servers are connected and false otherwise.
     */
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

    /**
     * Formats the response of the server.
     *
     * @param in The buffered reader with the server response.
     * @return The formatted response.
     */
    private static String getStringFromBufferedReader(BufferedReader in) throws IOException {
        StringBuilder sb = new StringBuilder();
        try {
            String line = in.readLine();
            while (!StringUtils.equals(line, "END") && line != null) {
                sb.append(line).append("\n");
                line = in.readLine();
            }
        } catch (SocketException e) {
            return "BAD SERVER";
        }

        if (sb.isEmpty()) {
            return sb.toString();
        } else {
            return sb.substring(0, sb.length() - 1);
        }
    }

}
