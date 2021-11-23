package nl.utwente.utgo.quests;

import android.util.Log;
import android.util.Pair;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.utwente.utgo.Firestore;

/**
 * This class models a Puzzle, there should be 1 or more of this within a quest
 * Each puzzle has its own location, answer, hints, starting prompt (story/question)
 * and ways to model different types of puzzles:\n
 * the type property specifies whether the puzzles uses 3D AR objects, augmented images, or neither.
 * The puzzle then also contains a corresponding data structure to represent the 3D AR objects or
 * augmented image pairs for each player.
 */
public class Puzzle {
    private Quest enclosingQuest;
    private LatLng location;
    private String correctAnswer;
    private boolean correctlyAnswered;
    private List<String> prompts;
    private PUZZLETYPE type;
    private int order;
    private Map<Integer, List<String>> hintMap;
    private Map<Integer, List<Pair<String, String>>> augImgUrlsMap;
    private Map<Integer, List<AR3DObject>> ar3DObjectsMap;
    private String story;
    private boolean canSkip = false;

    public static final String TAG = "PuzzleClass";

    @Deprecated
    private List<Pair<String, String>> augImgUrls;
    @Deprecated
    private List<String> hints;
    @Deprecated
    private List<AR3DObject> ar3DObjects;
    @Deprecated
    private String prompt;

    /**
     * Creates a new Puzzle object, only mandatory parameter is the enclosing Quest object that the
     * puzzle belongs to.
     * @param enclosing - the enclosing Quest object
     */
    public Puzzle(Quest enclosing) {
        enclosingQuest = enclosing;
        hints = new ArrayList<>();
        augImgUrls = new ArrayList<>();
        augImgUrlsMap = new HashMap<>();
        ar3DObjects = new ArrayList<>();
        correctAnswer = "";
    }

    /**
     * @return - the enclosing Quest object
     */
    public Quest getEnclosingQuest() { return enclosingQuest; }

    /**
     * @return - the location of the puzzle
     */
    public LatLng getLocation() { return location; }

    /**
     * @return - the correct answer to the puzzle
     */
    public String getCorrectAnswer() { return correctAnswer; }

    /**
     * @return - the order of this puzzle within the enclosing Quest object
     */
    public int getOrder() { return order; }

    /**
     * @return - true if the puzzle has been correctly answered before, false if not
     */
    public boolean done() { return correctlyAnswered; }

    /**
     * Gets the prompt that belongs to the given player number
     * @param playernum - place in team
     * @return - corresponding prompt
     */
    public String getPrompt(int playernum) { return prompts.get(playernum % prompts.size()); }

    /**
     * @return - a list of all prompts (3 in total)
     */
    public List<String> getPrompts() { return prompts; }

    /**
     * @return - the story/question associated with this puzzle
     */
    @Deprecated
    public String getPrompt() { return prompt; }

    /**
     * @param n - the place of the player within their team
     * @return - the hint associated with them
     */
    @Deprecated
    public String getHint(int n) { return hints.get(n % hints.size()); }

    /**
     * @return - all hints for all players
     */
    @Deprecated
    public List<String> getAllHints() { return hints; }

    /**
     * Gets the list of hints associated with player n
     * @return - the hints associated with the player
     */
    public List<String> getHints() {
        return hintMap.get(Firestore.getQuestRole() % hintMap.keySet().size()); }

    /**
     * @return - return the raw map of all hints
     */
    public Map<Integer, List<String>> getHintMap() { return hintMap; }

    /**
     * @return - the type of puzzle (Location-based/augmented image/regular)
     */
    public PUZZLETYPE getType() { return type; }

    /**
     * @return - the list of all augmented image pairs for all players
     */
    @Deprecated
    public List<Pair<String, String>> getAugmentedImagePairs() {
        return augImgUrls;
    }

    /**
     * @return - the list of images to be scanned for each player
     */
    @Deprecated
    public List<String> getAugmentedImageScans() {
        List<String> res = new ArrayList<>();
        for (Pair<String, String> p : augImgUrls) {
            res.add(p.first);
        }
        return res;
    }

    @Deprecated
    public List<String> getAugmentedImageResponses() {
        List<String> res = new ArrayList<>();
        for (Pair<String, String> p : augImgUrls) {
            res.add(p.second);
        }
        return res;
    }

    /**
     * @return - a mapping of the player's place in the team to a list of augmented image pairs
     */
    public Map<Integer, List<Pair<String, String>>> getAugmentedImageUrlMap() { return augImgUrlsMap; }


    /**
     * @return - the 3D AR object to be spawned for each player
     */
    @Deprecated
    public List<AR3DObject> getAr3DObjects() { return ar3DObjects; }

    /**
     * @return - the mapping of 3D objects for each player
     */
    public Map<Integer, List<AR3DObject>> getAr3DObjectsMap() { return ar3DObjectsMap; }

    /**
     * @return - gets the story prompt that should be displayed before the puzzle
     */
    public String getStory() { return story; }
    //setters below here

    /**
     * Sets the hint for each player
     * @param hintarr - array of 3 hints (1 for each player)
     * @return - itself after setting the hints
     */
    @Deprecated
    public Puzzle setHints(List<String> hintarr) {
        hints = hintarr;
        return this;
    }

    /**
     * Sets the hints for each player
     * @param map - mapping of player number to their corresponding hints
     * @return - itself after setting the hints
     */
    public Puzzle setHintMap(Map<Integer, List<String>> map) {
        hintMap = map;
        return this;
    }

    /**
     * Sets the order of the puzzle within the enclosing Quest
     * @param ord - order of the puzzle
     * @return - itself after setting the order
     */
    public Puzzle setOrder(int ord) {
        order = ord;
        return this;
    }

    /**
     * Sets the 3D AR object for each player
     * @param ar3DObjects - the list of AR3DObjects
     * @return - itself after setting the AR3DObjects
     */
    @Deprecated
    public Puzzle setAR3DObjects(List<AR3DObject> ar3DObjects) {
        this.ar3DObjects = ar3DObjects;
        return this;
    }

    /**
     * Sets the augmented image pair for each player
     * @param urls - urls to the augmented images in pairs
     * @return - itself after setting
     */
    @Deprecated
    public Puzzle setAugImage(List<Pair<String, String>> urls) {
        augImgUrls = urls;
        return this;
    }

    /**
     * Sets the mapping of the player's place -> list of augmented image pairs
     * @param map - the correct mapping
     * @return - itself after setting
     */
    public Puzzle setAugmentedImageMap(Map<Integer, List<Pair<String, String>>> map) {
        augImgUrlsMap = map;
        return this;
    }

    /**
     * Sets the type of the puzzle
     * @param t - the type of puzzle
     * @return - itself after setting the type
     */
    public Puzzle setType(PUZZLETYPE t) {
        type = t;
        return this;
    }

    /**
     * Sets the prompt (story/question)
     * @param p - desired prompt
     * @return - itself after setting the prompt
     */
    @Deprecated
    public Puzzle setPrompt(String p) {
        prompt = p;
        return this;
    }

    /**
     * Sets the prompts (story/questions)
     * @param prompts - the desired list of prompts
     * @return - itself after setting the prompts
     */
    public Puzzle setPrompts(List<String> prompts) {
        this.prompts = prompts;
        return this;
    }

    /**
     * Sets the correct answer if it's not been set yet
     * @param ans - correct answer
     * @return - itself
     */
    public Puzzle setCorrectAnswer(String ans) {
        if (correctAnswer == "") {
            if (ans == null) {
                canSkip = true;
                correctAnswer = "";
            } else {
                correctAnswer = ans;
            }
        }
        return this;
    }

    /**
     * Sets the location that the Puzzle should be in
     * @param loc - location of puzzle
     * @return - itself after setting the location
     */
    public Puzzle setLocation(LatLng loc) {
        location = loc;
        return this;
    }

    /**
     * Sets the map of AR3DObjects, mapping place in team -> list of 3d objects
     * @param arobjects - the desired mapping of 3d objects
     * @return - itself after setting the 3d object map
     */
    public Puzzle setAR3DObjectsMap(Map<Integer, List<AR3DObject>> arobjects) {
        this.ar3DObjectsMap = arobjects;
        return this;
    }

    /**
     * Sets
     * @param story
     * @return
     */
    public Puzzle setStory(String story) {
        this.story = story;
        return this;
    }

    /**
     * used for submitting an answer, marks the puzzle as done() if correct
     * CASE INSENSITIVE
     * @param ans - submitted answer
     * @return - true if answer is correct
     */
    public boolean checkAnswer(String ans) {
        boolean pCorrectlyAnswered = ans.toLowerCase().equals(correctAnswer.toLowerCase());
        if (pCorrectlyAnswered) skip();
        correctlyAnswered = pCorrectlyAnswered;
        return correctlyAnswered;
    }

    /**
     * Skips the puzzle
     */
    public void skip() {
        if (order == enclosingQuest.getPuzzles().size()-1) {
            //quest is done
            if (enclosingQuest instanceof XpQuest) {
                XpQuest test = (XpQuest) enclosingQuest;
                int xp = test.getXp();
                Firestore.updateXp(xp);
            }
            Log.i(TAG, "Quest :" + enclosingQuest.getTitle() + " has been completed!");
        } else {
            //setpuzzle to next puzzle
            enclosingQuest.getActivity().getPlayFragment()
                    .setPuzzle(enclosingQuest.getPuzzle(order+1));
        }
    }

    public boolean isSkippable() {
        return canSkip;
    }

    /**
     * Indicates what type of puzzle
     */
    public enum PUZZLETYPE {
        LOCATION_BASED,
        AUGMENTED_IMAGE,
        REGULAR
    }
}
