# ChatSystem

The Chat System is separated into the following classes/files:

## Server:
> ChatServer.java [contains main]<br/>ServerCommunicationClient.java<br/>DataStore.java

## Client:
> ChatClient.java [contains main]<br/>StyleSheet.css<br/>GUIController.java<br/>ClientCommunicationLogic.java<br/>InputValidation.java

To use the GUI (please note that there is only a GUI for the client), start the program using java ChatServer, along with the command line flag "-gui", for example, "java ChatClient -gui". The address and port can then be entered in a pop-up window. Please note: Any Address or Port entered on the command line will not be used if accompanied by the "-gui" flag. if nothing is entered in the setup window, defaults of "Localhost" and 14001 will be used.

The GUI contains a close button which leaves the chat without disturbing the server and typing EXIT into the message box closes the server.

When an exception occurs when the GUI is running, I have made the decision not to stop the client as this gives time for users to read any error message and terminate the program at their own leisure. This is not the case for the command line, where the program is terminated after an exception if needed.

Finally, to explore CSS and themes in JavaFX, I have made a "dark mode" in my GUI, which is activated by clicking the button at the bottom of the screen.
