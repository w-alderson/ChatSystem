/**
 * Main connection with clients
 * Responsible for establishing the connection and removing it once it is no longer active
 */


import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class ChatServer {

    private ServerSocket serverSocket;
    private ArrayList<ServerCommunicationLogic> clientCommunication = new ArrayList<>();
    private ServerCommunicationLogic serverCommunicationLogic;
    private DataStore dataStore;


    /**
     * Creates new DataStore and ServerCommunication object and starts ServerSocket for each new connection. The
     * ServerCommunicationLogic object is added to the clientConnection ArrayList.
     * @param port The desired port to set up the ServerSocket to.
     */
    public ChatServer(int port){

        dataStore = new DataStore();
        try {
            serverSocket = new ServerSocket(port);
            while (true) {
                Socket socket = serverSocket.accept();
                serverCommunicationLogic = new ServerCommunicationLogic(socket, this, dataStore);
                clientCommunication.add(serverCommunicationLogic);
                }
        }
        catch(BindException e){
            System.out.println("Connection failed. Please try another or try again later.");
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Handles "EXIT" command and then calls the clientCommunication objects to send a message to each client
     */
    public synchronized void messageToAll() {
        if(dataStore.getLastMessage().split(": ")[1].equals("EXIT")){
            System.out.println("Server closing by command of user.");
            serverCommunicationLogic.stringToClient(dataStore.getLastMessage().split(": ")[0] + " has chosen" +
                    " to close the server");
            if(!serverSocket.isClosed()){
                try {
                    serverSocket.close();
                }
                catch(SocketException e){
                    System.out.println("Socket Closed");
                }
                catch(IOException e){
                    System.out.println("Socket Closed");
                }
            }
            System.exit(0);
        }
        else {
            if(!dataStore.getLastMessage().equals(null)) {
                for (int i = 0; i < clientCommunication.size(); i++) {
                    (clientCommunication.get(i)).stringToClient(dataStore.getLastMessage());
                }
            }
        }
    }

    /**
     * Removes objects corresponding to disconnected users and therefore shuts down their threads. Re-adjusts
     * dataStore lists to mirror dis-connection.
     * @param toBeRemoved the object that has been disconnected
     */
    public void removeConnection(ServerCommunicationLogic toBeRemoved){
        clientCommunication.remove(toBeRemoved);
        dataStore.userNotActive(toBeRemoved);
    }

    public static void main(String[] args) {

        int port = 14001;
        boolean portRead;

        //Handles command line flag for port
        if (args.length > 0) {
            if(args[0].equals("-csp")) {
                    portRead = true;
                    try {
                        port = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        System.out.println("The port entered is invalid. Using 14001 as default.");
                    } catch (ArrayIndexOutOfBoundsException e){
                        System.out.println("No port found after flag. Default port 14001 is being used.");
                    }
                    if(port < 0 || port > 65535){
                        port = 14001;
                        System.out.println("The port entered is invalid. Using 14001 as default.");
                    }
                if(!portRead) {
                    System.out.println("Invalid command line flag.");
                }

            }
        }

        new ChatServer(port);
    }

}
