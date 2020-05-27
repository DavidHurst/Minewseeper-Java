package minesweeper3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Class representative of the View and Controller components of application
 * structure, generates and updates a GUI allowing user input to be interpreted,
 * causing the appropriate methods to be called from the classes that comprise
 * the Model component of the application structure; MineTile.java ,
 * Minefield.java and GameState.java, styling of components achieved through
 * styleSheet.css.
 *
 * @author 198780
 */
public class Minesweeper extends Application {

    BorderPane root, infoContainer, infoPane;
    GridPane gamePane;
    Label[][] tiles;
    Minefield minefield;
    MenuBar menuBar;
    Image flag, mine, tile, sadFace, happyFace, coolDude, oDude;
    boolean timerRunning;
    Label scoreDisplay, timeDisplay, face;
    MediaPlayer winAudio, lossAudio, ouch;
    Timer gameTimer;
    Media partyHorn, aww, oof;

    @Override
    public void start(Stage primaryStage) {
        root = new BorderPane();
        scoreDisplay = new Label();
        flag = new Image("flag.png");
        mine = new Image("mine.png");
        tile = new Image("tile.png");
        happyFace = new Image("happyFace.png");
        sadFace = new Image("sadFace.png");
        coolDude = new Image("coolDude.png");
        oDude = new Image("oDude.png");
        timerRunning = false;
        gameTimer = new Timer();

        String partyHornURI = "resources/party-horn.mp3";
        String awwURI = "resources/aww.wav";
        String oofURI = "resources/oof.mp3";
        partyHorn = new Media(new File(partyHornURI).toURI().toString());
        aww = new Media(new File(awwURI).toURI().toString());
        oof = new Media(new File(oofURI).toURI().toString());

        // Initialise game with a new Minefield and initialise infor display.
        infoContainer = initialiseInfoPane();
        gamePane = initialiseGame(new Minefield(10, 15, 20));

        root.setTop(infoContainer);
        root.setCenter(gamePane);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(Minesweeper.class.getResource(
                "styleSheet.css").toExternalForm());

        primaryStage.setTitle("Minesweeper");
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(mine);
        primaryStage.show();
    }

    /*
     * Overriding application close to stop the timer if it is running and 
     * terminate any threads it may have left running.
     */
    @Override
    public void stop() {
        if (timerRunning) {
            stopTimer();
        }
        System.exit(0);
    }


    /*
     * Starts game timer.
     */
    private void startTimer() {
        try {
            // Use Timer to schedule the gameTime field on the Minefield object 
            // to increase by 1 every second.
            gameTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    minefield.incGameTime();
                    // Enqueues setting the text of timeDisplay in the JavaFX 
                    // event queue to make the call non-blocking. 
                    Platform.runLater(() -> timeDisplay.setText(formatTime(
                            minefield.getGameTime())));
                }
            }, 0, 1000);
            // If Timer is cancelled and not already running, instantiate new 
            // timer and attempt to start it again.
        } catch (IllegalStateException excep) {
            gameTimer = new Timer();
            if (!timerRunning) {
                startTimer();
            }
        }
    }

    /*
     * Formats given seconds into a minutes:seconds format and returns as a
     * String.
     * 
     * @param seconds seconds to convert to mm:ss String
     * @return String representation of seconds input in mm:ss format
     */
    private String formatTime(int seconds) {
        Pattern twoDigits = Pattern.compile("\\d\\d");
        // Wrap seconds round at 60 and calculate minutes from seconds.
        int secs = seconds % 60;
        int mins = seconds / 60;
        String finalSecs = "" + secs;
        String finalMins = "" + mins;

        // Only applies to seconds and minutes < 10 but ensures string always 
        // has at least two digits, prefixing a 0 to the string if not.
        Matcher validSecs = twoDigits.matcher("" + secs);
        if (!validSecs.matches()) {
            finalSecs = "0" + finalSecs;
        }
        Matcher validMins = twoDigits.matcher("" + mins);
        // Only check up to 99 mins to allow mins over 100.
        if (!validMins.matches() && mins < 100) {
            finalMins = "0" + finalMins;
        }
        return finalMins + " : " + finalSecs;
    }

    /*
     * Stops the game timer.
     */
    private void stopTimer() {
        gameTimer.cancel();
        timerRunning = false;
    }

    /*
     * Initialises game from given Minefield object, using the dimensions of the
     * Minefield to instantiate the correct size 2D array of Labels to represent 
     * MineTiles. Also starts the game timer, populates the Minefield and 
     * updates the GUI.
     * 
     * @param m Minefield object to initialise game from and with
     * @return instantiated GridPane representative of Minefield
     */
    private GridPane initialiseGame(Minefield m) {
        gamePane = new GridPane();
        minefield = m;
        int rows = minefield.getRows();
        int cols = minefield.getCols();
        tiles = new Label[rows][cols];
        face.setGraphic(new ImageView(happyFace));

        minefield.populate();
        gamePane.setAlignment(Pos.CENTER);
        gamePane.setDisable(false);
        gamePane.setId("GameArea");

        // Create Labels representing tiles and add them to GridPane with 
        // appropriate index. 
        // N.B.: Gridpane constraints reversed to align with Minefield indexing.
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                tiles[i][j] = new Label();
                formatLabel(tiles[i][j]);
                GridPane.setConstraints(tiles[i][j], j, i);
                gamePane.getChildren().add(tiles[i][j]);
            }
        }
        initialiseCommands();
        // Only start timer once per game.
        if (!timerRunning) {
            startTimer();
            timerRunning = true;
        }
        update();
        return gamePane;
    }

    /*
     * Formats size and content display of given label.
     *
     * @param l Label to format
     */
    private void formatLabel(Label l) {
        l.setMinSize(50.0, 50.0);
        l.setContentDisplay(ContentDisplay.CENTER);
        l.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    /*
     * Instantiates and returns a BorderPane which holds the score Label,  
     * MenuBar, game timer and game face.
     * 
     * @return BorderPane containing game information.
     */
    private BorderPane initialiseInfoPane() {
        infoPane = new BorderPane();
        infoContainer = new BorderPane();
        scoreDisplay = new Label();
        timeDisplay = new Label("00 : 00");
        face = new Label();

        scoreDisplay.setPrefSize(130, 55);
        timeDisplay.setPrefSize(130, 55);

        face.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        face.setOnMouseClicked(e -> {
            ouch = new MediaPlayer(oof);
            ouch.play();
        });

        infoPane.setRight(timeDisplay);
        infoPane.setCenter(face);
        infoPane.setLeft(scoreDisplay);
        infoPane.setId("InfoArea");

        infoContainer.setTop(createMenu());
        infoContainer.setCenter(infoPane);

        return infoContainer;
    }

    /*
     * Instantiates and returns the game menu containing options for; creating 
     * a new game, saveing and loading game and quitting.
     * 
     * @return the game menu
     */
    private MenuBar createMenu() {
        menuBar = new MenuBar();
        menuBar.setId("Menu");
        Menu gameMenu = new Menu("Game");
        MenuItem newGame = new MenuItem("New Game");
        MenuItem quit = new MenuItem("Quit");
        MenuItem save = new MenuItem("Save Game");
        MenuItem load = new MenuItem("Load Save");

        // Attempt to create new game from user input.
        newGame.setOnAction(e -> {
            newGame();
        });
        // Terminate application.
        quit.setOnAction(e -> {
            Platform.exit();
        });
        // Attempt to save current game to file.
        save.setOnAction(e -> {
            saveGame();
        });
        // Attempt to load game from file.
        load.setOnAction(e -> {
            loadSave();
        });

        gameMenu.getItems().addAll(newGame, save, load, quit);
        menuBar.getMenus().add(gameMenu);
        return menuBar;
    }

    /*
     * Attempts to serialise and write current Minefield object to file.
     */
    private void saveGame() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Load Failed");
        alert.setHeaderText(null);
        alert.setContentText("Failed to save to file.");
        alert.initStyle(StageStyle.UTILITY);

        // Try to serialise and write current game to file.
        try {
            minefield.save();
        } catch (IOException e) {
            alert.showAndWait();
        }

        // Display save success if file saved successfully.
        alert.setAlertType(Alert.AlertType.INFORMATION);
        alert.setContentText("Game saved successfully.");
        alert.showAndWait();
    }

    /*
     * Attempts to read read and de-serialise object stored in file, if 
     * successful initialises game with object from file.
     */
    private void loadSave() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Load Failed");
        alert.setHeaderText(null);
        alert.setContentText("Failed to load save.");
        alert.initStyle(StageStyle.UTILITY);
        Minefield savedGame = null;

        // Try to deserialise and read stored game from file.
        try {
            savedGame = minefield.load();
        } catch (FileNotFoundException excep) {
            alert.setContentText("Failed to load save - no save found.");
            alert.showAndWait();
        } catch (IOException | ClassNotFoundException excep) {
            alert.showAndWait();
        }

        // Display load success and initilaise game with object from file.
        if (savedGame != null) {
            root.setCenter(initialiseGame(savedGame));
            // If game is already finished, disable input and stop timer.
            if (!minefield.getGameState().equals(GameState.ONGOING)) {
                gamePane.setDisable(true);
                stopTimer();
            }
            alert.setAlertType(Alert.AlertType.INFORMATION);
            alert.setContentText("Game loaded.");
            alert.showAndWait();
        }
    }

    /*
     * Delineate which commands are called when a MouseEvent is raised on a
     * Label i.e. interprets user input and calls appropriate method, also
     * updates GUI and checks if game is won or lost.
     */
    private void initialiseCommands() {
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[0].length; j++) {
                final int l = i;
                final int k = j;
                tiles[i][j].setOnMouseClicked(e -> {
                    // If left-click step on tile.
                    if (e.getButton().equals(MouseButton.PRIMARY)) {
                        minefield.step(l, k);
                        // If right-click mark tile. 
                    } else if (e.getButton().equals(MouseButton.SECONDARY)) {
                        minefield.toggleMarkTile(l, k);
                    }
                    // Check if all user has won and update GUI.
                    minefield.areAllMinesMarked();
                    update();
                    isGameOver(minefield.getGameState());
                });
                // Show surprised face when mouse pressed, return to smiling 
                // when released.
                tiles[i][j].setOnMousePressed(e -> {
                    face.setGraphic(new ImageView(oDude));
                });
                tiles[i][j].setOnMouseReleased(e -> {
                    face.setGraphic(new ImageView(happyFace));
                });
            }
        }
    }

    /*
     * Attempts to initialise new game from output of getNewMinefield().
     */
    private void newGame() {
        // Generate new Minefield, if null, display failure to create new game
        // and exit method.
        Minefield newMinefield = getNewMinefield();
        if (newMinefield == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to create new game.");
            alert.initStyle(StageStyle.UTILITY);
            alert.showAndWait();
            return;
        }

        // Generate new Minefield from user input and replace old gamePane with
        // newly generated one.
        root.setCenter(initialiseGame(newMinefield));
        minefield.resetGameTime();
    }

    /*
     * Generates custom Dialog with 3 TextFields to allow user to enter  
     * values for rows, columns and mines to instantiate a new Minefield with,
     * returns Minefield with given dimensions or null if inputs are invalid.   
     * 
     * @return Minefield with specified parameters or null if inputs invalid
     */
    private Minefield getNewMinefield() {
        // Format layout of dialog and add control elements.
        Dialog<Minefield> dialog = new Dialog<>();
        ButtonType create = new ButtonType("Create Game", ButtonData.OK_DONE);
        TilePane tilePane = new TilePane();
        Pattern digitOnly = Pattern.compile("\\d\\d?\\d?"); // 1-3 digits
        TextField[] inputs = new TextField[3];

        Label rowsLabel = new Label("Rows: ");
        Label colsLabel = new Label("Columns: ");
        Label minesLabel = new Label("Mines: ");
        for (int i = 0; i < 3; i++) {
            inputs[i] = new TextField();
        }

        tilePane.setPrefColumns(2);
        tilePane.setTileAlignment(Pos.CENTER_LEFT);
        tilePane.getChildren().addAll(rowsLabel, inputs[0], colsLabel,
                inputs[1], minesLabel, inputs[2]);

        dialog.getDialogPane().setId("dialog");
        dialog.getDialogPane().setContent(tilePane);
        dialog.setTitle("New Game Set-up");
        dialog.setHeaderText("Enter how many Rows, Columns and \nMines you "
                + "would like the game to have:");
        dialog.getDialogPane().getButtonTypes().add(create);
        dialog.initStyle(StageStyle.UTILITY);

        // Assign result of Dialog to be a new Minefield object with parameters
        // parsed from TextFields or null if inputs are invalid.
        dialog.setResultConverter((ButtonType b) -> {
            // If TextFields are are empty or not exclusively numerical, set 
            // result to return null. Also null for any input greater than 999.
            // N.B.: Pre-processing of Minefield inputs done in Minefield 
            // constructor.
            for (TextField t : inputs) {
                Matcher validText = digitOnly.matcher(t.getText());
                if (!validText.matches()) {
                    return null;
                }
            }
            // Parse ints from TextFields.
            int rows = Integer.parseInt(inputs[0].getText());
            int cols = Integer.parseInt(inputs[1].getText());
            int mines = Integer.parseInt(inputs[2].getText());

            // Return new Minefield on button pressed or null if dialog closed.
            return b == create ? new Minefield(rows, cols, mines) : null;
        });

        // Obtain result, if result is not null return result (Minefield)
        Optional<Minefield> result = dialog.showAndWait();

        if (result.isPresent()) {
            return result.get();
        }
        return null;
    }

    /*
     * Updates Labels in gamePane by displaying the image which correlates to 
     * the current state of the MineTile stored at the corresponding index.
     */
    private void update() {
        int markedMines = 0;
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[0].length; j++) {
                // Check state of MineTile object corresponding to tile and
                // update display on Label.
                String state = minefield.getMineTiles()[i][j].toString();
                switch (state) {
                    // Display mine image on Label if MineTile is mined and
                    // revealed.
                    case " * ":
                        tiles[i][j].setGraphic(new ImageView(mine));
                        break;
                    // Display flag image on Label if MineTile is marked.
                    case "[?]":
                        tiles[i][j].setGraphic(new ImageView(flag));
                        break;
                    // Display tile image when tile unmarked and unrevealed.
                    case "[ ]":
                        tiles[i][j].setGraphic(new ImageView(tile));
                        break;
                    // If none of the above, display mined neighbours.
                    default:
                        tiles[i][j].setContentDisplay(ContentDisplay.TEXT_ONLY);
                        tiles[i][j].setText(state);
                }
                // Count number of correctly marked tiles.
                if (minefield.getMineTiles()[i][j].correctlyMarked()) {
                    markedMines++;
                }
            }
        }
        // Update score label with number of mines left to mark.
        scoreDisplay.setText("" + (minefield.getMinesPlaced() - markedMines));
    }

    /*
     * Determine if game is over from given state. If game is over, determine
     * won or lost and display appropriate audio and message, prevent any more
     * input to the game and set appropriate emotion on face.
     * 
     * @param state current state of the game
     */
    private void isGameOver(GameState state) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.initStyle(StageStyle.UTILITY);
        // If user has won, output congratulating audio and visuals. 
        if (state.equals(GameState.WON)) {
            face.setGraphic(new ImageView(coolDude));
            stopTimer();
            gamePane.setDisable(true);
            playWinAudio();
            alert.setHeaderText("All mines marked correctly, You Win!");
            alert.setContentText("Final Score: " + finalScore());
            alert.showAndWait();
        }

        // If user has lost, output commiserating audio and visuals.
        if (state.equals(GameState.LOST)) {
            face.setGraphic(new ImageView(sadFace));
            stopTimer();
            gamePane.setDisable(true);
            playLossAudio();
            alert.setAlertType(Alert.AlertType.ERROR);
            alert.setHeaderText("You stepped on a mine, You Lose!");
            alert.setContentText("Use the Game Menu to start a new game.");
            alert.showAndWait();
        }
    }

    /*
     * Calculates and returns a users final score based on how long they took to
     * beat the game and how difficult the game was.
     * 
     * @return final score 
     */
    private int finalScore() {
        int difficulty = (minefield.getCols() * minefield.getRows())
                / minefield.getMaxMines();
        // x20 to boost user self-esteem.
        return (difficulty * 20) - minefield.getGameTime();
    }

    /**
     * Plays congratulatory audio.
     */
    private void playWinAudio() {
        winAudio = new MediaPlayer(partyHorn);
        winAudio.play();
    }

    /*
     * Plays commiserating audio.
     */
    private void playLossAudio() {
        lossAudio = new MediaPlayer(aww);
        lossAudio.play();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
