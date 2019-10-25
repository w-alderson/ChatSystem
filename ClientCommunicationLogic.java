/**
 * Contains the main thread logic for communication with the server
 */

import javafx.application.Platform;
import java.io.*;
import java.net.*;

public class ClientCommunicationLogic {

    private PrintWriter toServer;
    private Socket socket;
    private GUIController guiController;
    private boolean usingGUI;
    private ChatClient chatClient;

    /**
     * Initialises objects
     * @param socket An existing socket set up in either Chat-Client or GUIController
     * @param usingGUI determines whether the program should tailor the logic to a GUI or not
     */
    public ClientCommunicationLogic(Socket socket, boolean usingGUI) {

        this.socket = socket;
        this.usingGUI = usingGUI;

    }

    /**
     * Locic for client-server communication
     * @param userInput The message to be sent
     */
    public void runOut(String userInput) {

        try {
            toServer = new PrintWriter(socket.getOutputStream(), true);
        }
        catch(SocketException e){
            return;
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        //If the text after the username is "EXIT"
        if (userInput.split(": ")[1].equals("EXIT")) {
            if (usingGUI) {
                Platform.runLater(() -> {
                    if (guiController.exit("Are you sure?", "Are you sure you want to close the server? " +
                            "This will remove all users from the chat.")) {
                        //This allows the server to deal with the EXIT command to follow the rest of the program
                        toServer.println(userInput);

                    }
                });
            }
            else{
                toServer.println(userInput);
            }
        }
        else {
            toServer.println(userInput);
        }
    }

    /**
     * Logic for server-client communication
     */
    public void runIn() {

        try {
            while (socket.getInputStream() != null && socket.isConnected()) {
                BufferedReader serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String serverRec = serverIn.readLine();
                if(serverRec == null){
                    out("The Server has stopped responding. Please restart and try again.");
                    cleanClose();
                }
                else {
                    out(serverRec);
                }
            }
        }
        catch(IOException e){
            out("The server is disconnected. Messages sent now will not be received by other users.");
        }
        catch (NullPointerException e) {
            out(" There is a problem connecting to the server. Please try again");
        }

    }

    /**
     * Setter for GUIController if the user is using a GUI
     * @param guiController a GUIController object
     */
    public void setGuiController(GUIController guiController) {
        this.guiController = guiController;
    }

    /**
     * Setter for ChatClient if the user is using the command line
     * @param chatClient
     */
    public void setChatClient(ChatClient chatClient){
        this.chatClient = chatClient;
    }

    /**
     * Allows the program to close using the correct close method in the respective GUI/Non-GUI Class
     */
    public void cleanClose() {
        if (usingGUI) {
            guiController.closeProgram();
        } else {
            chatClient.closeProgram();
        }
    }

    /**
     * Displays a message to the user
     * @param s the message to be displayed
     */
    private synchronized void out(String s ) {
        if (usingGUI) {
            Platform.runLater(() -> {
                guiController.updateLabel(s);
            });
        } else {
            System.out.println(s);
        }
    }
}