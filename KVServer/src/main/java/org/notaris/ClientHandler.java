package org.notaris;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.notaris.tree.trie.Trie;
import org.notaris.tree.trie.TrieNode;
import org.notaris.tree.trie.TrieUtils;
import org.notaris.utils.ServerUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private final String user;
    private static final Logger logger = LogManager.getLogger(ClientHandler.class);
    private Trie mainDB;

    public ClientHandler(Socket clientSocket, Trie mainDB) {
        this.clientSocket = clientSocket;
        this.user = clientSocket.getInetAddress().toString().replace("/", "") + ":"
                + clientSocket.getLocalPort();
        this.mainDB = mainDB;
    }

    public void run() {
        try {
            // Get the input and output streams for the socket
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            DateFormat df = new SimpleDateFormat("mm:ss:SSS");

            // Continuously read data from the client and send a response
            while (true) {
                String inputLine = in.readLine();
                if (inputLine != null) {
                    inputLine = inputLine.trim();

                    logger.info("Command \"" + inputLine + "\" received from user " + user);

                    long startTime = System.currentTimeMillis();
                    String response = handleCommand(inputLine);
                    long endTime = System.currentTimeMillis();

                    out.println(response + "\n" + "Time to execute: " + df.format(endTime - startTime) + "\nEND");
                    out.flush();
                } else {
                    logger.info("Connection with " + user + " was disconnected");
                    in.close();
                    out.close();
                    clientSocket.close();
                    break;
                }
            }

        } catch (IOException e) {
            logger.info("Connection with " + user + " was disconnected unexpectedly");
        }
    }


    private String handleCommand(String userCommand) {

        String[] command = UserCommandUtils.readCommand(userCommand);
        String commandType = command[0];
        String rightPart = command[1];
        String response;

        switch (commandType) {
            case "PUT" -> {
                response = handlePut(rightPart);
            }
            case "GET" -> { // Searches only in top level keys
                response = handleGet(rightPart);
            }
            case "QUERY" -> {
                response = handleQuery(rightPart);
            }
            case "INDEX" -> {
                response = handleIndex(rightPart);
            }
            case "DELETE" -> {
                response = handleDelete(rightPart);
            }
            case "COMPUTE" -> {
                response = handleCompute(rightPart);
            }
            default -> response = "ERROR: " + commandType;
        }
        return response;
    }

    private String handlePut(String rightPart) {
        ServerUtils.saveKey(rightPart, mainDB);
        logger.info("KEY INSERTED");
        return "KEY INSERTED " + rightPart;
    }

    private String handleGet(String rightPart) {
        TrieNode keyValue = mainDB.find(rightPart);
        if (keyValue != null) {
            Trie keyToFind = (Trie) keyValue.getValue();
            return rightPart + " -> " + TrieUtils.getKey(keyToFind);
        } else {
            return "KEY WAS NOT FOUND";
        }
    }

    private String handleQuery(String rightPart) {
        Object key = ServerUtils.findKey(rightPart, mainDB);
        if (key != null && key instanceof Trie) {
            return rightPart + " -> " + TrieUtils.getKey((Trie) key);
        } else if (key != null && key instanceof String) {
            return rightPart + " -> " + key;
        } else {
            return "KEY WAS NOT FOUND";
        }
    }

    private String handleIndex(String rightPart) {
        Set<String> indexFile = IO.readFile(rightPart);
        for (String _key : indexFile) {
            ServerUtils.saveKey(_key, mainDB);
            logger.info("IMPORTED KEY" + _key);
        }
        logger.info("FILE INDEXED");
        return "FILE INDEXED";
    }

    private String handleDelete(String rightPart) {
        boolean keyDeleted = ServerUtils.delete(rightPart, mainDB);
        if (keyDeleted) {
            return "KEY DELETED SUCCESSFULLY";
        } else {
            return "THERE WAS AN ERROR DELETING KEY: " + rightPart;
        }
    }

    private String handleCompute(String rightPart) {

        String strExpression = rightPart.substring(0, rightPart.indexOf("WHERE ") - 1);

        String[] queryParameters = rightPart.substring(rightPart.indexOf("WHERE ") + 6, rightPart.length()).split("AND");

        HashMap<String, Double> parameters = new HashMap<>();

        for (String parameter : queryParameters) {
            String[] queryArray = parameter.split("=");
            String variable = queryArray[0].trim();
            String query = queryArray[1].replace("QUERY ", "").trim();

            String resolvedValue = handleQuery(query);
            String parsedResolvedValue = resolvedValue.substring(resolvedValue.indexOf(" -> ") + 4, resolvedValue.length());
            parameters.put(variable, Double.valueOf(parsedResolvedValue));
        }

        Expression expression = new ExpressionBuilder(strExpression)
                .variables(parameters.keySet())
                .build();

        for (Map.Entry<String, Double> entry : parameters.entrySet()) {
            expression.setVariable(entry.getKey(), entry.getValue());
        }

        return String.valueOf(expression.evaluate());
    }


}
