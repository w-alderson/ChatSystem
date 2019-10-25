/**
 * ChatClient contains the main logic for non-GUI command line use and the main method to allow the user to choose
 * between using a GUI or not
 */

import java.io.*;
import java.net.*;

public class ChatClient{

    private Socket socket;
    private ClientCommunicationLogic communicationLogic;
    private BufferedReader br;
    private String userInput;
    private String userName;



    /**
     * Constructor used when the user selects to use the command line version.
     * @param address The address for the ServerSocket, defaulted to "Localhost" if not selected using command line
     *                flag
     * @param port The port for the ServerSocket, defaulted to 14001 if not selected in using command line flag
     */
    public ChatClient(String address, int port) {

        //Sets up server connection
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(address, port), 1000);
        } catch (UnknownHostException e) {
            System.out.println("There is a problem connecting to the server. Please check and try again");
        } catch (ConnectException e) {
            System.out.println("There is a problem connecting to the server. Please check and try again");
            System.exit(0);
        } catch (IOException e) {
            System.out.println("There has been a timeout. Please check the IP address and port");
            System.exit(0);
        }

        //Accepts user input for the username, which is concatenated to messages sent to the server
        System.out.println("Please enter a UserName:");
        BufferedReader buffReader = new BufferedReader(new InputStreamReader(System.in));
        try {
            userName = buffReader.readLine();
        } catch (IOException e) {
        }

        System.out.println("Please start typing messages.");


        //Creates new object for ClientCommunicationLogic, which holds the bulk of the threads "run" methods
        communicationLogic = new ClientCommunicationLogic(socket, false);
        communicationLogic.setChatClient(this);

        br = new BufferedReader(new InputStreamReader(System.in));

        //Thread to handle client-server communication
        Thread outThread = new Thread(() -> {

            while (socket.isConnected()) {
                try {
                    //Read the user input
                    userInput = br.readLine();
                } catch (IOException e) {
                }
                communicationLogic.runOut(userName + ": " + userInput);
            }
        });

        //Thread to handle server-client communication
        Thread inThread = new Thread(() -> {
            communicationLogic.runIn();
        });

        inThread.start();
        outThread.start();

    }

    /**
     * Provides a clean way to close the client
     */
    public void closeProgram(){
        try {
            if (socket.isConnected()) {
                socket.close();
            }
        }
        catch(IOException e){}
        System.exit(0);
    }

    public static void main(String[] args) {

        //Pass args to validation to organise and start the relevant process
        InputValidation validation = new InputValidation(false);
        validation.handleArgs(args);

    }
}