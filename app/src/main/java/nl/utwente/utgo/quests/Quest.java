package nl.utwente.utgo.quests;

import com.google.android.gms.maps.model.LatLng;

import java.util.*;

import nl.utwente.utgo.MainActivity;

/**
 * Baseclass to support different types of Quests
 */
public abstract class Quest {
    private MainActivity main;
    private String title;
    private String description;
    private String color;
    private int nPuzzles;
    private int minimumPlayers;

    private LatLng location;
    private long until;

    private List<Puzzle> puzzles;

    /**
     * Constructor of Quest - creates an empty quest object
     * @param mainActivity - the MainActivity in which this Quest plays
     */
    public Quest(MainActivity mainActivity) {
        main = mainActivity;
        puzzles = new ArrayList<>();
    }

    /**
     * Second Constructor of Quest, creates a full Quest object.
     * @param mainActivity the MainActivity in which this Quest plays
     * @param paramPuzzles the different puzzles (in order) to play. Index 0 is the first puzzle to play
     * @param title the title of the Quest
     * @param description the description of the Quest
     * @param color the colour of the Quest, this colour is used on the map and quest tab
     * @param location the location of Quest, so where it starts.
     * @param until the amount of time remaining in the quest //TODO how does this work Remco v.D?
     */
    public Quest(MainActivity mainActivity, ArrayList<Puzzle> paramPuzzles,
                 String title, String description, String color, LatLng location, long until) {
        main = mainActivity;
        puzzles = paramPuzzles;
        this.title = title;
        this.description = description;
        this.color = color;
        this.location = location;
        this.until = until;
        this.nPuzzles = paramPuzzles.size();
    }
    /**
     * @return - the activity where this Quest is being used
     */
    public MainActivity getActivity() { return main; }

    /**
     * @return - the title of the Quest
     */
    public String getTitle() { return title; }

    /**
     * @return - the description of the Quest
     */
    public String getDescription() { return description; }

    /**
     * @return - the color of the Quest as a String of the form #(0-F)^6
     */
    public String getColor() { return color; }

    /**
     * @return - the location of the Quest within the real world
     */
    public LatLng getLocation() { return location; }

    /**
     * @return - the end time for this quest in milliseconds
     */
    public long getUntil() { return until;}

    /**
     * @return - the milliseconds remaining for this quest
     */
    public long getRemainingTime() { return until - (new Date().getTime()); }

    /**
     * @param order - the order of the puzzle to be returned
     * @return - the puzzle with specified order
     */
    public Puzzle getPuzzle(int order) { return puzzles.get(order); }

    /**
     * @return - the whole list of puzzles for this Quest
     */
    public List<Puzzle> getPuzzles() { return puzzles; }

    /**
     * @return - gets the number of puzzles as specified in the database
     */
    public int getnPuzzles() { return nPuzzles; }

    /**
     * @return - gets the minimum number of players required for this Quest
     */
    public int getMinimumPlayers() { return minimumPlayers; }

    /**
     * @param n - the number of puzzles as given in the database
     * @return - the Quest object after setting
     */
    public Quest setnPuzzles(int n) {
        nPuzzles = n;
        return this;
    }

    /**
     * @param min - the minimum number of players for this Quest
     * @return - itself after setting the minimum number of players
     */
    public Quest setMinimumPlayers(int min) {
        minimumPlayers = min;
        return this;
    }

    /**
     * @param title1 - the title of the Quest
     * @return - the Quest object after setting
     */
    public Quest setTitle(String title1) {
        title = title1;
        return this;
    }

    /**
     * @param desc - the description of the Quest
     * @return - the Quest object after setting
     */
    public Quest setDescription(String desc) {
        description = desc;
        return this;
    }

    /**
     * @param color1 - the color of the Quest (should be of the form #(0-F)^6
     * @return - the Quest object after setting
     */
    public Quest setColor(String color1) {
        color = color1;
        return this;
    }

    /**
     * @param loc - the location of the Quest in the real world
     * @return - the Quest object after setting
     */
    public Quest setLocation(LatLng loc) {
        location = loc;
        return this;
    }

    /**
     * @param mil - the amount of milliseconds until the Quest expires
     * @return - the Quest object after setting
     */
    public Quest setUntil(long mil) {
        until = mil;
        return this;
    }

    /**
     * @param puzzleList - the list of puzzles associated with this Quest
     * @return - the Quest object after setting
     */
    public Quest setPuzzles(List<Puzzle> puzzleList) {
        puzzles = puzzleList;
        return this;
    }

    /**
     * Adds a single puzzle to the list of puzzles
     * @param puzzle - the puzzle to be added
     * @return - the Quest object after adding the puzzle
     */
    public Quest addPuzzle(Puzzle puzzle) {
        puzzles.add(puzzle);
        return this;
    }
}
