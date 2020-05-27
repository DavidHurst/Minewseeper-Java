package minesweeper3;

import java.io.Serializable;


/**
 * Class which creates MineTile objects that have; a field to indicate if the 
 * tile is mined, a field to indicate number of mined neighbours the tile has, 
 * a field to indicate if the tile has been revealed and a field to indicate if 
 * the user has marked the tile.
 *
 * @author DavidHurst
 */
class MineTile implements Serializable {

    private boolean isMined, isRevealed, isMarked;
    private int minedNeighbours;

    /**
     * Constructs a MineTile object.
     */
    public MineTile() {
        this.isMined = false;
        this.minedNeighbours = 0;
        this.isRevealed = false;
        this.isMarked = false;
    }

    /**
     * Returns a string representation of the current state of the MineTile
     * depending on the current state of the MineTile's fields.
     *
     * @return String representation of the MineTile's current state.
     */
    @Override
    public String toString() {
        String display;
        if (this.isRevealed) {
            if (this.isMined) {
                display = " * ";
            } else {
                display = " " + minedNeighbours + " ";
            }
        } else {
            display = "[ ]";
        }
        if (this.isMarked && !this.isRevealed) {
            display = "[?]";
        }
        return display;
    }

    /*
     * Increments mined neighbours field.
     */
    protected void incrementMinedNeighbours() {
        this.minedNeighbours++;
    }

    /*
     * Returns boolean indicating whether MineTile is mined or not.
     *
     * @return boolean indicating whether MineTile is mined or not.
     */
    protected boolean getIsMined() {
        return this.isMined;
    }

    /*
     * Sets isMined field to specified value.
     *
     * @param value Value to set isMined field to.
     */
    protected void setIsMined(boolean value) {
        this.isMined = value;
    }

    /*
     * Returns int indicating number of mined neighbours MineTile has.
     *
     * @return int indicating how many mined neighbours MineTile has.
     */
    protected int getMinedNeighbours() {
        return this.minedNeighbours;
    }

    /*
     * Returns boolean indicating if MineTile has been isRevealed or not.
     *
     * @return boolean indicating if MineTile has been isRevealed or not.
     */
    protected boolean getIsRevealed() {
        return this.isRevealed;
    }

    /*
     * Returns boolean indicating if MineTile has been marked by user or not.
     *
     * @return boolean indicating if MineTile has been marked by user or not.
     */
    protected boolean getIsMarked() {
        return this.isMarked;
    }

    /*
     * Sets isRevealed to specified value. If revealing tile then also set tile
     * to be un-marked.
     *
     * @param value Value to set isRevealed to.
     */
    protected void setIsRevealed(boolean value) {
        if (value && this.isMarked) {
            this.isMarked = false;
        }
        this.isRevealed = value;
    }

    /*
     * Inverts the isMarked field.
     */
    protected void toggleIsMarked() {
        
        this.isMarked = !this.isMarked;
    }
    
    /*
     * Returns boolean indicating if MineTile is correctly marked.
     * @return true if MineTile is marked and mined
     */
    protected boolean correctlyMarked() {
        return (isMarked && isMined);
    }

}
