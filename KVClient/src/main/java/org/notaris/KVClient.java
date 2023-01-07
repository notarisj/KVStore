package org.notaris;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KVClient {

    private static final Logger logger = LogManager.getLogger(KVClient.class);

    public static void main(String[] args) throws IOException {
        // Create a Scanner to read input from the command line
        Scanner scanner = new Scanner(System.in);
        DateFormat df = new SimpleDateFormat("mm:ss:SSS");

        // args = new String[] {"-s", "C:\\Users\\Notaris\\Desktop\\serverFile.txt", "-i", "C:\\Users\\Notaris\\Desktop\\dataToIndex.txt",  "-k", "2"};

        // Read user arguments
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
            List<ServerStruct> servers = ClientUtils.connectToServers(serverFile);

            // 2. Index Data
            long startTime = System.currentTimeMillis();
            boolean indexOk = ClientUtils.indexData(dataToIndex, servers, replicationFactor);
            long endTime = System.currentTimeMillis();

            if (indexOk) {
                logger.info("Index finished successfully");
            } else {
                logger.info("Something went wrong while indexing");
            }
            logger.info("Time to execute: " + df.format(endTime - startTime));

            System.out.print("\n------------------ \033[32m\033[1mWelcome to KVClient\033[0m ------------------\n");
            System.out.print("Enter a command to send to the server (or '\033[1mexit\033[0m' to quit)\n\n");

            // 3. Accept user command and loop until the user types exit
            while (true) {
                /*
                Shuffle to send command to random servers. The number of
                servers is based on the command. For example delete is sent
                to all servers.
                 */
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

                String response = ClientUtils.handleCommand(userCommand, servers, replicationFactor);
                System.out.println(response);
            }
        } else {
            logger.info("No arguments where provided. Quiting KVClient...");
        }
    }
}