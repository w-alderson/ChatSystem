/**
 * Contains the threads for server-client communication
 */

import java.net.*;
import java.io.*;

public class ServerCommunicationLogic {

    private PrintWriter clientOut;
    private String userInput;
    private InputStreamReader reader;
    private BufferedReader clientIn;

    /**
     * Adds a new user to the dataStore and defines and starts an input and output thread
     * @param socket the socket set up in ChatServer
     * @param server an object of ChatServer
     * @param dataStore an object of DataStore
     */
    public ServerCommunicationLogic (Socket socket, ChatServer server, DataStore dataStore){

        dataStore.addUser(this);

        //Defining a server-client thread
        Thread outThread = new Thread(() -> {

            try {
                clientOut = new PrintWriter(socket.getOutputStream(), true);
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        });

        //Defining a client-server thread
        Thread inThread = new Thread(() -> {
                try {
                    reader = new InputStreamReader(socket.getInputStream());
                    clientIn = new BufferedReader(reader);
                    while (socket.getInputStream()!= null && socket.isConnected()) {
                        userInput = clientIn.readLine();
                        if(userInput == null){
                            throw new IOException();
                        }
                        else {
                            synchronized (this) {
                                System.out.println(userInput);
                                dataStore.addMessage(userInput);
                                server.messageToAll();
                            }
                        }

                    }
                }
                catch (IOException e) {
                    server.removeConnection(this);
                }
            });

        outThread.start();
        inThread.start();

    }

    /**
     * Called from ChatServer in a loop to send the same message to all clients.
     * @param s the message to be sent to the client
     */
    public synchronized void stringToClient(String s){
        clientOut.println(s);
    }
}
