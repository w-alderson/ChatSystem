/**
 * Sets up the GUI and contains the logic for GUI-specific operations
 */

import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.scene.control.*;
import java.io.IOException;
import java.net.*;
import javafx.concurrent.*;

public class GUIController extends Application {

    private Stage window;
    private Button darkButton;
    private Label titleLabel;
    private TextField textInput;
    private HBox topBarLayout;
    private GridPane bottomBarLayout;
    private static boolean answer;
    private String userInput, backgroundString;
    private Socket socket;
    private ClientCommunicationLogic communicationLogic;
    private Scene scene;
    private ScrollPane scroll;
    private Text text;
    private Service<Void> backgroundTaskOut;
    private Service<Void> backgroundTaskIn;
    private String address;
    private int port;
    private boolean darkModeFlag;

    private String userName;

    /**
     * Creates threads to handle server communication and initialises select variables
     */
    public GUIController() {

        backgroundString = "Your messages will be displayed here: \n";
        text = new Text(backgroundString);

        //Default
        userName = "User";

        //Handles client-server communication for the GUI
        backgroundTaskOut = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        while (true) {
                            if (isCancelled()) {
                                return null;
                            } else {
                                //Sends userInput to server via runOut
                                communicationLogic.runOut(userInput);
                            }
                            try {
                                wait();
                            } catch (InterruptedException e) {
                            }

                        }
                    }
                };
            }
        };

        //Handles server-client communication
        backgroundTaskIn = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        if (isCancelled()) {
                            return null;
                        } else {
                            communicationLogic.runIn();
                        }
                        return null;
                    }
                };
            }
        };

    }


    /**
     * Sets up GUI elements such as windows and handles events on objects in this stage.
     *
     * @param primaryStage
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception {

        //Gets port, address and username when needed
        setup();

        window = primaryStage;
        primaryStage.setTitle("Chat Client User");

        darkModeSetup();
        bottomSetup();
        scrollSetup();
        titleSetup();

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(topBarLayout);
        borderPane.setBottom(bottomBarLayout);

        borderPane.setCenter(scroll);
        borderPane.setPadding(new Insets(10, 50, 50, 50));

        scene = new Scene(borderPane, 700, 550);
        window.setScene(scene);
        window.show();

        //Sets up socket after getting information from setup()
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(address, port), 1000);
        } catch (UnknownHostException e) {
            updateLabel("There has been a problem connecting to the server.");
        } catch (IOException e) {
            updateLabel("There has been a timeout. Please check the IP address and port.");
        }


        //Creates object which holds main logic for the threads
        communicationLogic = new ClientCommunicationLogic(socket, true);
        communicationLogic.setGuiController(this);

        backgroundTaskOut.start();
        backgroundTaskIn.start();

    }

    /**
     * Sets up the top section of the BorderPane (title)
     */

    public void titleSetup() {

        titleLabel = new Label("Chat Client");
        titleLabel.setFont(Font.font(45));
        titleLabel.setStyle("-fx-text-fill: #F42C04");
        topBarLayout = new HBox();
        topBarLayout.getChildren().addAll(titleLabel);
        topBarLayout.setAlignment(Pos.BASELINE_CENTER);
        topBarLayout.setPadding(new Insets(50, 50, 50, 50));

    }

    /**
     * Sets up the bottom section of the BorderPane (Send and Close buttons, textField)
     */
    public void bottomSetup() {

        Button sendButton = new Button("Send");
        Button closeButton = new Button("Close");
        textInput = new TextField();
        textInput.setPromptText("Type your message here");
        textInput.setPrefWidth(300);

        closeButton.setOnAction(e -> {
            exit("Are you sure?", "Are you sure you want to leave the chat?");
            if (answer) {
                closeProgram();
            }
        });
        sendButton.setOnAction(e -> {
            //Concatenates username to messages so the user know who sent each message
            userInput = userName + ": " + textInput.getText();
            textInput.clear();
            backgroundTaskOut.restart();
        });
        textInput.setOnAction(e -> {
            //Concatenates username to messages so the user know who sent each message
            userInput = userName + ": " + textInput.getText();
            textInput.clear();
            backgroundTaskOut.restart();
        });

        HBox test = new HBox();
        test.getChildren().addAll(darkButton);
        test.setAlignment(Pos.CENTER);


        //Adds all of the elements to a GridPane
        bottomBarLayout = new GridPane();
        bottomBarLayout.add(sendButton, 0, 0);
        bottomBarLayout.add(textInput, 1, 0);
        bottomBarLayout.add(closeButton, 2, 0);
        bottomBarLayout.add(test, 1, 1);
        bottomBarLayout.setAlignment(Pos.CENTER);
        bottomBarLayout.setHgap(20);
        bottomBarLayout.setVgap(20);
        bottomBarLayout.setPadding(new Insets(30, 30, 30, 30));
    }

    /**
     * Sets up a button to allow user to select dark mode and contains the logic to make the switch
     */
    public void darkModeSetup() {
        darkButton = new Button("Dark Mode");
        darkButton.setAlignment(Pos.BOTTOM_RIGHT);
        darkButton.setOnAction(e -> {
            if (darkButton.getText().equals("Dark Mode")) {
                scene.getStylesheets().add("Stylesheet.css");
                titleLabel.setStyle("-fx-text-fill: #326273; -fx-text-bold: true ");
                scroll.setStyle("-fx-text-color:#84828F; -fx-background-color: #262322");
                text.setFill(Color.GRAY);
                darkButton.setText("Light Mode");
                darkModeFlag = true;
            } else {
                scene.getStylesheets().clear();
                text.setFill(Color.BLACK);
                titleLabel.setStyle("-fx-text-fill: #F42C04");
                darkModeFlag = false;
                darkButton.setText("Dark Mode");
            }

        });

    }

    /**
     * Sets up the central part of the BorderPane and contains the constraints on the scrollPane where messages are
     * displayed
     */
    public void scrollSetup() {

        scroll = new ScrollPane();
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        scroll.setPrefSize(450, 270);
        scroll.setContent(text);

    }


    /**
     * Creates a pop up window to ensure the user wants to close the server
     *
     * @param title   The title of the window
     * @param message The message displayed in a label the window
     * @return true for confirmation of exit, false otherwise
     */
    public boolean exit(String title, String message) {

        Stage exitWindow = new Stage();
        exitWindow.setTitle(title);
        exitWindow.setMinWidth(250);
        Label label = new Label(message);

        Button yesButton = new Button("Yes");
        yesButton.setOnAction(e -> {
            answer = true;
            exitWindow.close();
        });

        Button noButton = new Button("No");
        noButton.setOnAction(e -> {
            answer = false;
            exitWindow.close();
        });


        //Adds all elements to the scene
        HBox hb = new HBox(50);
        VBox vb = new VBox(10);
        hb.getChildren().addAll(yesButton, noButton);
        hb.setAlignment(Pos.CENTER);
        vb.getChildren().addAll(label, hb);
        vb.setAlignment(Pos.CENTER);
        Scene exitScene = new Scene(vb);
        vb.setPadding(new Insets(10, 10, 10, 10));
        exitWindow.setScene(exitScene);
        exitWindow.showAndWait();

        return answer;
    }

    /**
     * Allows the user to select the address, port and a username using a pop up window before the main GUI runs.
     * If nothing is selected at this stage, defaults are chosen and the main application will run.
     */
    public void setup() {

        port = 14001;
        address = "Localhost";

        Stage setupWindow = new Stage();
        setupWindow.setTitle("Setup");
        setupWindow.setMinWidth(100);
        setupWindow.setMinHeight(100);
        Label label1 = new Label("Please enter the required information: ");
        Label label2 = new Label("Note: if nothing is entered these will be set to default values.");
        label1.setAlignment(Pos.CENTER);
        Label addressLabel = new Label("Address: ");
        Label portLabel = new Label("Port: ");
        Label nameLabel = new Label("Chat name:");

        TextField addressInput = new TextField();
        TextField portInput = new TextField();
        TextField nameInput = new TextField();
        addressInput.setPrefWidth(200);
        addressInput.setPrefHeight(20);
        portInput.setPrefWidth(200);
        portInput.setPrefHeight(20);
        nameInput.setPrefWidth(200);
        nameInput.setPrefHeight(20);

        addressInput.setPromptText("Localhost");
        portInput.setPromptText("14001");
        nameInput.setPromptText("User");

        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> {
            InputValidation validation = new InputValidation(true);
            validation.setGUIController(this);
            //If the TextFields are empty, keep the defaults, otherwise choose the new ones
            if (!addressInput.getText().trim().isEmpty()) {
                address = validation.convertIPFormat(addressInput.getText());
                System.out.println(address);
            }

            if (!portInput.getText().trim().isEmpty()) {
                port = validation.convertPort(portInput.getText());
            }
            if (!nameInput.getText().trim().isEmpty()) {
                //Remove a : from the end of the name as this will cause complications later
                userName = nameInput.getText().split(": ")[0];
            }
            validation = null;

            setupWindow.close();


        });

        GridPane gp = new GridPane();

        //Add all elements to the GridPane
        gp.setColumnSpan(label1, 2);
        gp.add(label1, 0, 0);
        gp.add(addressLabel, 0, 1);
        gp.add(addressInput, 1, 1);
        gp.add(portLabel, 0, 2);
        gp.add(portInput, 1, 2);

        gp.add(nameLabel, 0, 3);
        gp.add(nameInput, 1, 3);
        gp.add(submitButton, 0, 4);
        gp.add(label2, 1, 4);

        gp.setHgap(20);
        gp.setVgap(20);
        gp.setPadding(new Insets(20, 20, 20, 20));
        gp.setAlignment(Pos.CENTER);

        Scene setupScene = new Scene(gp);
        setupWindow.setScene(setupScene);
        setupWindow.showAndWait();

    }

    /**
     * A clean way to close the client by first closing the socket (if open) and then the window.
     */
    public void closeProgram() {
        try {
            backgroundTaskIn.cancel();
            backgroundTaskOut.cancel();
            window.close();
            if (socket != null) {
                if (socket.isConnected()) {
                    socket.close();
                }
            }
            System.exit(0);
        } catch (IOException e) {

        }
        System.exit(0);
    }

    /**
     * Adds new messages to the textField
     *
     * @param message The message to be added
     */
    public void updateLabel(String message) {

        try {
            backgroundString = backgroundString.concat(message + "\n");
        } catch (NullPointerException e) {
            backgroundString = backgroundString.concat("There is a problem connecting to the server.");
        }

        text = new Text(backgroundString);

        //If dark mode is in use, change the colour of the text
        if (darkModeFlag) {
            text.setFill(Color.GRAY);
        }
        scroll.setContent(text);
    }
}