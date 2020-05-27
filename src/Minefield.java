package minesweeper3;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Random;

/**
 * Class which creates a Minefield object that is a grid of MineTile objects.
 * The grid can be populated with MineTiles, displayed textually and MineTiles
 * can be mined and marked. Numbers represent how many mines are around a tile,
 * " * " represents a MineTile which has been mined, "[ ]" represents a MineTile
 * which has not been revealed and "[?]" represents a MineTile which has been
 * marked. Object also serialisable and can be saved and loaded from a file.
 *
 * @author DavidHurst
 */
public class Minefield implements Serializable {

    private final MineTile[][] mineTiles;
    private int maxMines, minesPlaced;
    private final Random randomNum;
    public final static String SAVE_FILE = "saveFile.txt";
    private int gameTime;
    private GameState gameState;

    /**
     * Constructs a Minefield object with the specified number of rows, columns
     * and maximum number of mines.
     *
     * @param numOfRows How many rows the Minefield will have.
     * @param numOfColumns How many columns the Minefield will have.
     * @param maxNumOfMines Maximum number of mines the Minefield can have.
     */
    public Minefield(int numOfRows, int numOfColumns, int maxNumOfMines) {
        // If specifed size of mineTiles is invalid, default to 10x10 Minefield.
        if (numOfRows < 1 || numOfColumns < 1) {
            numOfRows = 10;
            numOfColumns = 10;
        }
        this.mineTiles = new MineTile[numOfRows][numOfColumns];
        this.randomNum = new Random();
        this.minesPlaced = 0;
        // Fill Minefield with new MineTiles
        for (MineTile[] row : mineTiles) {
            for (int i = 0; i < row.length; i++) {
                row[i] = new MineTile();
            }
        }
        // If specified maximum number of mines invalid, default to a quarter 
        // the number of available tiles.
        if (maxNumOfMines <= 0 || maxNumOfMines
                > (int) (mineTiles.length * mineTiles[0].length)) {
            maxMines = (int) (mineTiles.length * mineTiles[0].length) / 4;
        } else {
            maxMines = maxNumOfMines;
        }
        gameTime = 0;
        gameState = GameState.ONGOING;
    }

    /**
     * Returns a string representing the current state of the Minefield with all
     * MineTiles revealed.
     *
     * @return A string representing the current state of the Minefield.
     */
    public String toStringRevealed() {
        String display = "";
        for (MineTile[] row : mineTiles) {
            for (MineTile tile : row) {
                tile.setIsRevealed(true);
                display += tile;
            }
            display += "\n";
        }
        return display;
    }

    /**
     * Returns a string representing the current state of the Minefield with all
     * MineTiles hidden.
     *
     * @return A string representing the current state of the Minefield.
     */
    public String toStringHidden() {
        String display = "";
        for (MineTile[] row : mineTiles) {
            for (MineTile tile : row) {
                tile.setIsRevealed(false);
                display += tile;
            }
            display += "\n";
        }
        return display;
    }

    /**
     * Returns a string representing the current state of the Minefield with all
     * MineTiles in their current state.
     *
     * @return A string representing the current state of the Minefield.
     */
    @Override
    public String toString() {
        String display = "";
        for (MineTile[] row : mineTiles) {
            for (MineTile tile : row) {
                display += tile;
            }
            display += "\n";
        }
        return display;
    }

    /**
     * Places mine on specified tile and increments mined neighbours of
     * specified tile if tile is mined successfully.
     *
     * @param row Row coordinate to mine.
     * @param column Column coordinate to mine.
     * @return Boolean indicating if tile was mined successfully.
     */
    protected boolean mineTile(int row, int column) {
        // Check tile to mine is not off the grid, maxMines has not been 
        // exceeded, the tile hasn't already been mined and tile isn't (0,0).
        if (row < 0 || column < 0 || row > mineTiles.length - 1
                || column > mineTiles[0].length - 1
                || minesPlaced >= maxMines || (row == 0 && column == 0)
                || mineTiles[row][column].getIsMined()) {
            return false;
        } else {
            // Mark tile as mined and increment minedNeighbours and minesPlaced.
            mineTiles[row][column].setIsMined(true);
            incrementNeighbours(row, column);
            minesPlaced++;
        }
        return true;
    }

    /**
     * Places the specified (in constructor) amount of mines randomly across the
     * Minefield.
     */
    public void populate() {
        // Loop until spcified amount of mines have been placed.
        while (minesPlaced < maxMines) {
            // Select random row and column and attempt to mine tile at that
            // location.
            int randRow = randomNum.nextInt(mineTiles.length);
            int randColumn = randomNum.nextInt(mineTiles[0].length);
            mineTile(randRow, randColumn);
        }
    }

    /*
     * Increments the neighbours of a MineTile.
     *
     * @param row Row coordinate of MineTile who's neighbours will be
     * incremented.
     * @param column Row coordinate of MineTile who's neighbours will be
     * incremented.
     */
    private void incrementNeighbours(int row, int column) {
        int x, y;
        // Loop through offsets.
        for (int rOffset = -1; rOffset < 2; rOffset++) {
            for (int cOffset = -1; cOffset < 2; cOffset++) {
                // Apply offsets to coordintes to be incremented.
                x = row + rOffset;
                y = column + cOffset;
                // If neighbour is not off the minefield or itself, increment. 
                if (!(x < 0 || y < 0 || x > mineTiles.length - 1
                        || y > mineTiles[0].length - 1
                        || (x == row && y == column))) {
                    mineTiles[x][y].incrementMinedNeighbours();
                }
            }
        }
    }

    /*
     * Returns integer indicating the maximum number of mines that can be
     * placed, specified when object is instantiated.
     *
     * @return Integer indicating the maximum mines that can be placed.
     */
    protected int getMaxMines() {
        return this.maxMines;
    }

    /*
     * Returns an int[][] array in which numbers represent how many mined
     * neighbours a tile has.
     *
     * @return Integer array representing how many mined neighbours each tile
     * has.
     */
    protected MineTile[][] getMineTiles() {
        return this.mineTiles;
    }

    /*
     * Returns an integer indicating how many mines have been placed.
     *
     * @return Integer indicating how many mines have been placed.
     */
    protected int getMinesPlaced() {
        return minesPlaced;
    }

    /*
     * Toggles marking a MineTile.
     *
     * @param row Row coordinate of MineTile to mark.
     * @param column Column coordinate of MineTile to mark.
     */
    protected void toggleMarkTile(int row, int column) {
        if (row < 0 || column < 0 || row > mineTiles.length - 1
                || column > mineTiles[0].length - 1
                || mineTiles[row][column].getIsRevealed()) {
            System.out.println("[ERROR] Failed to mark tile.");
        } else {
            mineTiles[row][column].toggleIsMarked();
        }
    }

    /**
     * Steps on a tile, if mined returns false meaning the user loses the game.
     * If user steps on an unmined tile with 0 mined neighbours that tile's
     * neighbours are searched. Tiles are revealed if they have 1 or more
     * minedNeighbours and only tiles with 0 minedNeighbours are searched
     * further.
     *
     * @param row Row coordinate to step on.
     * @param column Column coordinate to step on.
     * @return boolean indicating if user stepped on a mine.
     */
    public boolean step(int row, int column) {
        // If invalid input, output error message.
        if (row < 0 || column < 0 || row > mineTiles.length - 1
                || column > mineTiles[0].length - 1) {
            System.out.println("[ERROR] Failed to step on tile.");
            return true;
            // If tile stepped on is mined, return false i.e. game over.
        } else if (mineTiles[row][column].getIsMined()) {
            // Reveal all mined tiles for player to see how close/far they came
            for (MineTile[] col : mineTiles) {
                for (MineTile tile : col) {
                    if (tile.getIsMined()) {
                        tile.setIsRevealed(true);
                    }
                }
            }
            gameState = GameState.LOST;
            return false;
        }
        // Tile is unmined and is revealed.
        mineTiles[row][column].setIsRevealed(true);
        // If tile has no mined neighbbours, search and reveal all appropriate 
        // neighbours.
        if (mineTiles[row][column].getMinedNeighbours() == 0) {
            searchNeighboursToReveal(row, column);
        }
        return true;
    }

    /*
     * Recursivley searches neighbours of a tile that has been stepped on to
     * determine if they should be searched (i.e. if they have no mined 
     * neighbours). Neighbours of these tiles with 1 or more minedNeighbours
     * will be revealed themselves but not searched.
     */
    private void searchNeighboursToReveal(int row, int column) {
        int x, y;
        // Loop through offsets.
        for (int rOffset = -1; rOffset < 2; rOffset++) {
            for (int cOffset = -1; cOffset < 2; cOffset++) {
                // Apply offsets to coordintes to be searched.
                x = row + rOffset;
                y = column + cOffset;
                // If neighbour is not off the minefield, or the tile that has 
                // been steped on (i.e. already revealed)... 
                if (!(x < 0 || y < 0 || x > mineTiles.length - 1
                        || y > mineTiles[0].length - 1
                        || (x == row && y == column))) {
                    // Base case for recusrion to terminate
                    if (!mineTiles[x][y].getIsRevealed()) {
                        mineTiles[x][y].setIsRevealed(true);
                        // If neighbour has no mined neighbours, search its
                        // neighbours recursivley
                        if (mineTiles[x][y].getMinedNeighbours() == 0) {
                            searchNeighboursToReveal(x, y);
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks all tiles and indicates if any are mined but not marked or marked
     * but not mined.
     *
     * @return boolean indicating if any tiles are mined but not marked or
     * marked but not mined.
     */
    public boolean areAllMinesMarked() {
        // Check all tiles
        for (MineTile[] row : mineTiles) {
            for (MineTile tile : row) {
                // If tile is marked but not mined or tile is mined but not 
                // marked, return false.
                if (tile.getIsMarked() && !tile.getIsMined()
                        || tile.getIsMined() && !tile.getIsMarked()) {
                    return false;
                }
            }
        }
        gameState = GameState.WON;
        return true;
    }

    /**
     * Returns the number of rows the minefield has.
     *
     * @return number of rows
     */
    public int getRows() {
        return mineTiles.length;
    }

    /**
     * Returns the number of columns the minefield has.
     *
     * @return number of columns
     */
    public int getCols() {
        return mineTiles[0].length;
    }

    /*
     * Attempts to serialise and write Minefield instance to file.
     */
    protected void save() throws IOException {
        try {
            ObjectOutputStream outStream = new ObjectOutputStream(
                    new FileOutputStream(SAVE_FILE));
            outStream.writeObject(this);
            outStream.close();
        } catch (IOException excep) {
            throw new IOException("Failed to write minefield to file." + excep);
        }
    }

    /*
     * Attempts to read and de-serialise object from file and return.
     * @return the de-serialised object from the file
     */
    protected Minefield load() throws FileNotFoundException, IOException,
            ClassNotFoundException {
        Minefield m = null;
        try {
            ObjectInputStream inStream = new ObjectInputStream(
                    new FileInputStream(SAVE_FILE));
            m = (Minefield) inStream.readObject();
            inStream.close();
        } catch (FileNotFoundException excep) {
            throw new FileNotFoundException("Failed to load from file" + excep);
        } catch (IOException excep) {
            throw new IOException("Failed to load from file" + excep);
        } catch (ClassNotFoundException excep) {
            throw new ClassNotFoundException("Failed to load from file"
                    + excep);
        }
        return m;
    }

    /*
     * Returns the game time of the Minefield.
     * @return the game time
     */
    protected int getGameTime() {
        return gameTime;
    }

    /*
     * Increments game time by one.
     */
    protected void incGameTime() {
        this.gameTime++;
    }

    /*
     * Sets game time to 0.
     */
    protected void resetGameTime() {
        this.gameTime = 0;
    }

    /*
     * Returns current game state.
     * @return Enum representing the current game state
     */
    protected GameState getGameState() {
        return this.gameState;
    }
}
