package edu.uob;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;

public final class GameServer {

    private static final char END_OF_TRANSMISSION = 4;


    public static void main(String[] args) throws IOException {

        StringBuilder entityFilePath  = new StringBuilder();
        entityFilePath.append("config");
        entityFilePath.append(File.separator);
        entityFilePath.append("extended-entities.dot");

        File entitiesFile = Paths.get(entityFilePath.toString()).toAbsolutePath().toFile();
        System.out.println(entitiesFile.getAbsolutePath());

        StringBuilder actionFilePath  = new StringBuilder();
        actionFilePath.append("config");
        actionFilePath.append(File.separator);
        actionFilePath.append("extended-actions.xml");


        File actionsFile = Paths.get(actionFilePath.toString()).toAbsolutePath().toFile();

        GameServer server = new GameServer(entitiesFile, actionsFile);
        server.blockingListenOn(8888);
    }

    /**
    * Do not change the following method signature or we won't be able to mark your submission
    * Instanciates a new server instance, specifying a game with some configuration files
    *
    * @param entitiesFile The game configuration file containing all game entities to use in your game
    * @param actionsFile The game configuration file containing all game actions to use in your game
    */


    private Game game;
    public GameServer(File entitiesFile, File actionsFile) {
        // Step 1 - ingestor instatntiated and used to create a new 'Game' object

        IngestionEngine ingestor = new IngestionEngine(entitiesFile, actionsFile);
        if(ingestor.getIngestionState()) {
            // Will only attempt to run the game if ingestion is successful
            this.game = ingestor.getGame();
            this.game.setGameStart();
        }

    }

    /**
    * Do not change the following method signature or we won't be able to mark your submission
    * This method handles all incoming game commands and carries out the corresponding actions.</p>
    *
    * @param command The incoming command to be processed
    */
    public String handleCommand(String command) {
        CommandProcessor newCommand = new CommandProcessor(command, this.game);
        return newCommand.processCommand();
    }

    /**
    * Do not change the following method signature or we won't be able to mark your submission
    * Starts a *blocking* socket server listening for new connections.
    *
    * @param portNumber The port to listen on.
    * @throws IOException If any IO related operation fails.
    */
    public void blockingListenOn(int portNumber) throws IOException {
        try (ServerSocket s = new ServerSocket(portNumber)) {
            StringBuilder serverListening = new StringBuilder();
            serverListening.append("Server listening on port ");
            serverListening.append(portNumber);

            System.out.println(serverListening);
            while (!Thread.interrupted()) {
                try {
                    this.blockingHandleConnection(s);
                } catch (IOException e) {
                    System.out.println("Connection closed");
                }
            }
        }
    }

    /**
    * Do not change the following method signature or we won't be able to mark your submission
    * Handles an incoming connection from the socket server.
    *
    * @param serverSocket The client socket to read/write from.
    * @throws IOException If any IO related operation fails.
    */
    private void blockingHandleConnection(ServerSocket serverSocket) throws IOException {
        try (Socket s = serverSocket.accept();
        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {
            System.out.println("Connection established");
            String incomingCommand = reader.readLine();
            if(incomingCommand != null) {
                StringBuilder response = new StringBuilder();
                response.append("Received message from ");
                response.append(incomingCommand);

                System.out.println(response);
                String result = this.handleCommand(incomingCommand);
                writer.write(result);

                StringBuilder transmission = new StringBuilder();
                transmission.append("\n");
                transmission.append(END_OF_TRANSMISSION);
                transmission.append("\n");

                writer.write(transmission.toString());
                writer.flush();
            }
        }
    }
}
