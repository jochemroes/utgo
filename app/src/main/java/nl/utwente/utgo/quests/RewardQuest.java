package nl.utwente.utgo.quests;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import nl.utwente.utgo.MainActivity;

/**
 * Subclass implementing the Quest baseclass.
 * RewardQuest represents a quest for which people get a specific reward if completed
 */
public class RewardQuest extends Quest {
    private String rewardDesc;

    /**
     * Only invokes the superclass constructor
     * @param mainActivity - the activity in which this is being used
     */
    public RewardQuest(MainActivity mainActivity) {
        super(mainActivity);
    }

    /**
     * Second Constructor of RewardQuest, creates a full RewardQuest object.
     * @param mainActivity the MainActivity in which this Quest plays
     * @param paramPuzzles the different puzzles (in order) to play. Index 0 is the first puzzle to play
     * @param title the title of the Quest
     * @param description the description of the Quest
     * @param color the colour of the Quest, this colour is used on the map and quest tab
     * @param location the location of Quest, so where it starts.
     * @param millisRemaining the amount of time remaining in the quest //TODO how does this work Remco v.D?
     * @param rewardDesc the reward this quest is played over.
     */
    public RewardQuest(MainActivity mainActivity, ArrayList<Puzzle> paramPuzzles,
                       String title, String description, String color, LatLng location, long millisRemaining, String rewardDesc) {
        super(mainActivity, paramPuzzles, title, description, color, location, millisRemaining);
        this.rewardDesc = rewardDesc;
    }
    /**
     * @return - a description of the reward to be given when the Quest completes
     */
    public String getRewardDescription() {
        return rewardDesc;
    }

    /**
     * @param desc - the description of the reward
     * @return - the RewardQuest object after setting
     */
    public RewardQuest setRewardDescription(String desc) {
        rewardDesc = desc;
        return this;
    }
}
