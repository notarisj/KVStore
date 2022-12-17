package org.notaris;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.notaris.tree.trie.Trie;
import org.notaris.tree.trie.TrieNode;

import java.io.*;
import java.net.*;

import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KVServer {

    private static final Logger logger = LogManager.getLogger(KVServer.class);

    public static Trie mainDB = new Trie(new TrieNode(new HashMap<>()));

    public static void main(String[] args1) throws IOException {
        String ipAddress = "";
        int port = 0;

        String[] args = {"-a", "192.168.31.4", "-p", "9997"};


        // parse command-line arguments
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-a")) {
                ipAddress = args[i + 1];
            } else if (args[i].equals("-p")) {
                port = Integer.parseInt(args[i + 1]);
            }
        }

        // Create a new ServerSocket to listen for incoming connections on the specified port
        logger.info("KVServer is starting up...");

        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(ipAddress, port));

        // Create a thread pool with a fixed number of threads
        ExecutorService threadPool = Executors.newFixedThreadPool(10);

        logger.info("KVServer successfully started.");
        logger.info("KVServer is UP and listening on port: " + port);

        while (true) {

            // Accept an incoming connection from a client
            Socket clientSocket = serverSocket.accept();
            logger.info("A new user (" + clientSocket.getInetAddress().toString().replace("/", "") +
                    ":" + clientSocket.getLocalPort() + ") was connected");
            // Create a new thread to handle the client
            threadPool.submit(new ClientHandler(clientSocket, mainDB));
        }
    }

}
