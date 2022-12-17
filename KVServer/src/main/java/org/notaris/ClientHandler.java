package org.notaris;

import org.apache.commons.lang3.StringUtils;
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
                    String command = inputLine.contains(" ") ? inputLine.substring(0, inputLine.indexOf(' ')).trim() : inputLine.trim();
                    String rightPart = inputLine.contains(" ") ? inputLine.substring(inputLine.indexOf(' ') + 1).trim() : "";
                    String response = "";

                    long startTime = System.currentTimeMillis();
                    if (StringUtils.equals(command, "PUT")) {
                        ServerUtils.saveKey(rightPart, mainDB);
                        logger.info("KEY INSERTED");
                        response = "KEY INSERTED";
                    } else if (StringUtils.equals(command, "GET")) {
                        Object key = ServerUtils.findKey(rightPart, mainDB);


                        StringBuilder builder = new StringBuilder();
                        //TrieUtils.print(mainDB.getRoot(), builder, true);
                        HashMap<String, TrieNode> finalChildren = new HashMap<>();
                        TrieUtils.findChildren(mainDB.getRoot(), builder, finalChildren, true);


                        if (key != null && key instanceof Trie) {
                            response = rightPart + " -> " + TrieUtils.getKey((Trie) key);
                        } else if (key != null && key instanceof String){
                            response = rightPart + " -> " + key;
                        } else {
                            response = "KEY WAS NOT FOUND";
                        }

                    } else if (StringUtils.equals(command, "INDEX")) {
                        Set<String> indexFile = IO.readFile(rightPart);
                        for (String key : indexFile) {
                            ServerUtils.saveKey(key, mainDB);
                            logger.info("IMPORTED KEY" + key);
                        }
                        logger.info("FILE INDEXED");
                        response = "FILE INDEXED";
                    } else {
                        response = "ERROR: " + command;
                    }

                    long endTime = System.currentTimeMillis();

                    out.println(response + "\n" + "Time to execute: " + df.format(endTime - startTime) + "\n");
                    out.flush();
                    //if (in.ready()) in.skip(inputLine.length());
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
}
