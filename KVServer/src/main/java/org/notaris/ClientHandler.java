package org.notaris;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.notaris.tree.trie.Trie;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

class ClientHandler implements Runnable {
    private final Socket clientSocket;
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
        String commandType = command[0].toUpperCase();
        String rightPart = command[1];
        String response;

        switch (commandType) {
            case "PUT" -> response = ClientHandlerUtils.handlePut(rightPart, mainDB);
            case "GET" -> response = ClientHandlerUtils.handleGet(rightPart, mainDB);
            case "QUERY" -> response = ClientHandlerUtils.handleQuery(rightPart, mainDB);
            case "DELETE" -> response = ClientHandlerUtils.handleDelete(rightPart, mainDB);
            case "COMPUTE" -> response = ClientHandlerUtils.handleCompute(rightPart, mainDB);
            default -> response = "ERROR: " + commandType;
        }
        return response;
    }
}
