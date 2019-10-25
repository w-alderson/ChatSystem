/**
 * Class to store all messages received by the server and connection history
 * Many of the methods are synchronized to stop two clients from altering the store simultaneously
 */

import java.util.ArrayList;

public class DataStore {

    private ArrayList<String> messages = new ArrayList<>();
    private ArrayList<ServerCommunicationLogic> users = new ArrayList<>();
    private ArrayList<Boolean> connectionActive = new ArrayList<>();


    /**
     * Adds a message to the log of all messages
     * @param message the message to be added
     */
    public synchronized void addMessage(String message) {

        messages.add(message);
    }

    /**
     *
     * @return the last message to be sent
     */
    public synchronized String getLastMessage(){

        return messages.get(messages.size()-1);
    }

    /**
     * Adds a user to the list of all users
     * @param user the user to be added
     */
    public synchronized void addUser(ServerCommunicationLogic user){
        users.add(user);
        connectionActive.add(true);
        System.out.println("New connection! Number of connections: "+activeConnections());
    }

    /**
     * Make a user inactive on the list of all  by chancing the corresponding active value to false
     * @param user the user to make inactive
     */
    public synchronized void userNotActive(ServerCommunicationLogic user){
        connectionActive.set(users.indexOf(user),false);
        System.out.println("User removed. Number of connections: "+activeConnections());
    }

    /**
     * Counts all the active connections by looping through the connectionActive ArrayList
     * @return the number of active users currently connected
     */
    public int activeConnections(){
        int count = 0;
        for(int i = 0; i<connectionActive.size(); i++){
            if (connectionActive.get(i)) {
                count++;
            }
        }
        return count;
    }

}