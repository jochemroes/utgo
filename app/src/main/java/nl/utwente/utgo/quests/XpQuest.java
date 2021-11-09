package nl.utwente.utgo.quests;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import nl.utwente.utgo.MainActivity;

/**
 * Subclass implementing the Quest baseclass.
 * XpQuest represents a Quest where people get a certain amount if experience point when completed.
 */
public class XpQuest extends Quest {
    private int xp;

    /**
     * only invokes the superclass constructor
     * @param mainActivity - the activity in which this is being used
     */
    public XpQuest(MainActivity mainActivity) {
        super(mainActivity);
    }

    /**
     * Second Constructor of XpQuest, creates a full XpQuest object.
     * @param mainActivity the MainActivity in which this Quest plays
     * @param paramPuzzles the different puzzles (in order) to play. Index 0 is the first puzzle to play
     * @param title the title of the Quest
     * @param description the description of the Quest
     * @param color the colour of the Quest, this colour is used on the map and quest tab
     * @param location the location of Quest, so where it starts.
     * @param millisRemaining the amount of time remaining in the quest //TODO how does this work Remco v.D?
     * @param xp the amount of xp this Quest rewards the player and team with.
     */
    public XpQuest(MainActivity mainActivity, ArrayList<Puzzle> paramPuzzles,
                   String title, String description, String color, LatLng location, long millisRemaining, int xp) {
        super(mainActivity, paramPuzzles, title, description, color, location, millisRemaining);
        this.xp = xp;
    }
    /**
     * @return - the amount of experience points to be given when complete
     */
    public int getXp() {
        return xp;
    }

    /**
     * @param xp - the amount of experience points when completed
     * @return - XpQuest after setting
     */
    public XpQuest setXp(int xp) {
        this.xp = xp;
        return this;
    }
}
