/**
 * To organise arguments passed in when opening the program and act accordingly
 */

import javafx.application.Application;
import javafx.application.Platform;

public class InputValidation {

    private boolean usingGUI;
    private GUIController guiController;
    private String address;
    private int port;


    /**
     * Constructor
     * @param usingGUI true if GUI is selected. Used to tailor the response to certain actions
     */
    public InputValidation(boolean usingGUI) {

        this.usingGUI = usingGUI;

        //Default port and address
        port = 14001;
        address = "localhost";

    }

    /**
     * Verifies the format of IP addresses inputted by the user (Based on IPv4)
     * @param ipAddress inputted ip address
     * @return the new value of address
     */
    public String convertIPFormat(String ipAddress){
        if(ipAddress.equals("Localhost")){
            return ipAddress;
        }
        String[] splitAddress = ipAddress.split("\\.");
        String errorMessage = "Invalid IP Address inputted. \"Localhost\" is being used as default.";

        for(int i = 0; i < splitAddress.length; i++){
            try{
                if(0 > Integer.parseInt(splitAddress[i]) || Integer.parseInt(splitAddress[i]) >255){
                    out(errorMessage);
                    return "Localhost";
                }
            }
            catch(NumberFormatException e){
                out(errorMessage);
                return "Localhost";
            }
        }
        if(splitAddress.length > 4){
            out(errorMessage);
            return "Localhost";
        }
        if(ipAddress.endsWith(".")){
            out(errorMessage);
            return "Localhost";
        }

        return ipAddress;
    }


    /**
     * Converts the inputted port to something suited to the server
     * @param port the inputted port
     * @return the new value of the port
     */
    public int convertPort(String port){

        String errorMessage = "Invalid port inputted. 14001 is being used as default.";
        try{
            if(Integer.parseInt(port) < 0) {
                out(errorMessage);
                return 14001;
            }
        }
        catch(NumberFormatException e){
            out(errorMessage);
            return 14001;
        }
        return Integer.parseInt(port);
    }

    /**
     * Displays a message to the user
     * @param s the message to be displayed
     */
    private synchronized void out(String s ) {
        if (usingGUI) {
            //Add to the task to the JavaFX application thread queue to ensure the GUI is responsive
            Platform.runLater(() -> {
                guiController.updateLabel(s);
            });
        } else {
            System.out.println(s);
        }
    }

    /**
     * Sets a guiContoller object
     * @param guiController guiController object to be set
     */
    public void setGUIController(GUIController guiController){
        this.guiController = guiController;
    }

    /**
     * Looks at the command line arguments and determines the address, port and decision on whether to use a GUI and
     * acts accordingly.
     * @param args The arguments entered in the command line.
     */
    public void handleArgs(String[] args) {

        //Handles command line flags
        for (int i = 0; i < args.length; i++) {
            switch (args[i].charAt(0)) {
                //Looks for command line flags
                case '-':

                    if (args[i].equals("-ccp")) {
                        try {
                            port = convertPort(args[i + 1]);
                        } catch (ArrayIndexOutOfBoundsException e) {
                            out("No value after flag: \"-ccp\". A default value of 14001 is being used");
                        }
                    } else if (args[i].equals("-cca")) {
                        try {
                            address = convertIPFormat(args[i + 1]);

                        } catch (ArrayIndexOutOfBoundsException e) {
                            out("No value after flag: \"-cca\". A default value of \"Localhost\" is " +
                                    "being used");
                        }
                    }
                    else if(args[i].equals("-GUI")){
                        usingGUI = true;
                        //Sets up the GUI
                        Application.launch(GUIController.class, args);
                        //GUIController guiController = new GUIController(address, addressFlag, port, portFlag);
                    }
                    else {
                        out("Invalid flags used in command line. Defaults have been chosen for the " +
                                "address and port.");
                    }

            }
        }
        if(!usingGUI){
            new ChatClient(address, port);
        }

    }
}
