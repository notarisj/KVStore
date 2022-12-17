package org.notaris;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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

        String[] args = {"-s", "Z:\\IdeaProjects\\KVStore\\KVClient\\target\\serverFile.txt", "-i", "Z:\\IdeaProjects\\KVStore\\KVClient\\target\\dataToIndex.txt",  "-k",  "1"};

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


                server.out.write("PUT " + key + " \n");
                server.out.flush();
                System.out.println(server.in.readLine());
                //if (!StringUtils.equals(server.in.readLine(), "KEY INSERTED")) indexOk = false;

                String response = server.in.readLine();

                // Loop until there are no more lines to read
                while (!StringUtils.equals(response, "")) {
                    // Print the current line of input
                    System.out.println(response);

                    // Read the next line of input
                    response = server.in.readLine();
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
            List<SocketStruct> randomSockets = sockets.subList(0, replicationFactor);

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

                // Read the response from the server
                // Read the first line of input
                String response = server.in.readLine();

                // Loop until there are no more lines to read
                while (!StringUtils.equals(response, "")) {
                    // Print the current line of input
                    System.out.println(response);

                    // Read the next line of input
                    response = server.in.readLine();
                }
            }
        }
    }
}