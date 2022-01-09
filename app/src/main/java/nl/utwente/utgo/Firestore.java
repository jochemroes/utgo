package nl.utwente.utgo;

import android.annotation.SuppressLint;
import android.location.Location;
import android.util.Log;
import android.util.Pair;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.utwente.utgo.arcorelocation.sensor.DeviceLocation;
import nl.utwente.utgo.content.LeaderboardsContent;
import nl.utwente.utgo.content.SettingsContent;
import nl.utwente.utgo.profile.Dogroup;
import nl.utwente.utgo.profile.Group;
import nl.utwente.utgo.profile.Player;
import nl.utwente.utgo.profile.StudyAssociation;
import nl.utwente.utgo.profile.Team;
import nl.utwente.utgo.quests.Quest;
import nl.utwente.utgo.quests.RewardQuest;
import nl.utwente.utgo.quests.XpQuest;
import nl.utwente.utgo.quests.Puzzle.PUZZLETYPE;
import nl.utwente.utgo.quests.Puzzle;
import nl.utwente.utgo.quests.AR3DObject;

import static com.google.firebase.firestore.FieldPath.*;
import static com.google.firebase.firestore.FieldValue.increment;


/**
 * This final class interacts with Firebase Firestore by reading and writing data needed for the application.
 * The methods in this class should only be called statically.
 */
public final class Firestore {
    private static final String TAG = "Firestore";
    private static int questRole;

    // constants
    private static final int OWNER_ROLE = 2;
    private static final int DEFAULT_ROLE = 0;
    private static final int MAX_TEAM_SIZE = 3;

    // account deletion procedure -
    private static int groupCount;

    // collection references
    @SuppressLint("StaticFieldLeak")
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final CollectionReference userCol = db.collection("user");
    private static final CollectionReference teamCol = db.collection("team");
    private static final CollectionReference dogroupCol = db.collection("do_group");
    private static final CollectionReference studyCol = db.collection("study_association");
    private static final CollectionReference rewardCol = db.collection("reward_quest");
    private static final CollectionReference xpCol = db.collection("xp_quest");

    //quests
    protected static List<Quest> quests = new ArrayList<>();
    protected static List<RewardQuest> rewardQuests = new ArrayList<>();
    protected static List<XpQuest> xpQuests = new ArrayList<>();

    public static List<Quest> getQuestsList() {
        return quests;
    }

    //groups
    protected static List<Group> groups = new ArrayList<>();

    public static List<Group> getGroups() {
        return groups;
    }

    //quest fields in Firestore
    protected static final String QUEST_TITLE_FIELD = "title";
    protected static final String QUEST_DESCRIPTION_FIELD = "description";
    protected static final String QUEST_LOCATION_FIELD = "Loc";
    protected static final String QUEST_REWARD_FIELD = "reward";
    protected static final String QUEST_COLOR_FIELD = "color";
    protected static final String QUEST_XP_FIELD = "xp";
    protected static final String QUEST_UNTIL_FIELD = "until";
    protected static final String QUEST_NUM_PUZZLES = "npuzzles";
    protected static final String QUEST_MIN_PLAYERS = "minPlayers";

    //puzzle fields in Firestore
    @Deprecated
    protected static final String PUZZLE_LOC = "Loc";
    protected static final String PUZZLE_LOC_ARR = "locArr";
    @Deprecated
    protected static final String PUZZLE_3D_OBJ_URL = "3dObjUrl";
    @Deprecated
    protected static final String PUZZLE_AUG_IMG_URL = "augImgUrl";
    protected static final String PUZZLE_ANSWER = "answer";
    @Deprecated
    protected static final String PUZZLE_HINTS = "hints";
    protected static final String PUZZLE_HINT_MAP = "hintMap";
    protected static final String PUZZLE_ORDER = "order";
    @Deprecated
    protected static final String PUZZLE_PROMPT = "prompt";
    protected static final String PUZZLE_TYPE = "type";
    @Deprecated
    protected static final String PUZZLE_3D_OBJ_HEIGHTS = "3dObjHeights";
    @Deprecated
    protected static final String PUZZLE_CAN_TURN = "canTurn";
    protected static final String PUZZLE_AUG_IMG_URL_MAP = "augImgUrlMap";
    protected static final String PUZZLE_P1 = "p1";
    protected static final String PUZZLE_P2 = "p2";
    protected static final String PUZZLE_P3 = "p3";
    protected static final String PUZZLE_3D_OBJ_URL_MAP = "3dObjUrlMap";
    protected static final String PUZZLE_3D_OBJ_HEIGHTS_MAP = "3dObjHeightsMap";
    protected static final String PUZZLE_CAN_TURN_MAP = "canTurnMap";
    protected static final String PUZZLE_PROMPT_ARR = "promptArr";
    protected static final String PUZZLE_STORY = "story";

    // user uid
    protected static String userID;

    //puzzle types
    protected static final Long L_BASED = 0L;
    protected static final Long A_IMAGE = 1L;
    protected static final Long REG = 2L;
    protected static Map<Long, PUZZLETYPE> typeMap = new HashMap<>();

    // leaderboards
    private static List<Object[]> leaderboardPlayers;
    private static List<Object[]> leaderboardTeams;
    private static List<Object[]> leaderboardDogroups;
    private static List<Object[]> leaderboardAssociations;

    /**
     * Returns one of the leaderboard lists based on the query
     * @param query Enum that is related to one of the leaderboards
     * @return One of the leaderboards
     */
    public static List<Object[]> getLeaderboard(LeaderboardsContent.Query query) {
        switch (query) {
            case PLAYERS:
                return leaderboardPlayers;
            case TEAMS:
                return leaderboardTeams;
            case DOGROUPS:
                return leaderboardDogroups;
            case ASSOCIATIONS:
                return leaderboardAssociations;
            default:
                return null;
        }
    }


    //profile
    public static Player player;
    protected static Team team;
    protected static Dogroup dogroup;
    protected static StudyAssociation study;


    public static void createPuzzle(String uidQuest, double lat, double lon, String answer,
                                    ArrayList<String> hintList, int order, int type,
                                    ArrayList<String> augImgUrlList, ArrayList<String> objUrlList,
                                    ArrayList<Integer> objHeigthsList, ArrayList<Boolean> canTurnList,
                                    ArrayList<String> promptList, String story) {
        Map<String, Object> puzzleMap = new HashMap<>();

        ArrayList<String> emptyStringArray = new ArrayList<>();
        emptyStringArray.add("");

        ArrayList<Integer> emptyIntegerArray = new ArrayList<>();
        emptyIntegerArray.add(0);

        ArrayList<Boolean> emptyBooleanArray = new ArrayList<>();
        emptyBooleanArray.add(false);

        // empty values for deprecated fields
        ArrayList<String> objUrl = new ArrayList<>();
        objUrl.add("");
        objUrl.add("");
        objUrl.add("");

        ArrayList<String> augImgUrl = new ArrayList<>();
        augImgUrl.add("");
        augImgUrl.add("");
        augImgUrl.add("");
        augImgUrl.add("");
        augImgUrl.add("");
        augImgUrl.add("");

        ArrayList<String> hints = new ArrayList<>();
        hints.add("");
        hints.add("");
        hints.add("");

        ArrayList<Integer> heights = new ArrayList<>();
        heights.add(0);
        heights.add(0);
        heights.add(0);

        ArrayList<Boolean> canTurn = new ArrayList<>();
        canTurn.add(false);
        canTurn.add(false);
        canTurn.add(false);

        ArrayList<GeoPoint> locArr = new ArrayList<>();
        locArr.add(new GeoPoint(lat, lon));
        // filling puzzleMap
        puzzleMap.put(PUZZLE_LOC, new GeoPoint(lat, lon));
        puzzleMap.put(PUZZLE_LOC_ARR, locArr);
        puzzleMap.put(PUZZLE_3D_OBJ_URL, emptyStringArray);
        puzzleMap.put(PUZZLE_AUG_IMG_URL, emptyStringArray);
        puzzleMap.put(PUZZLE_ANSWER, answer);
        puzzleMap.put(PUZZLE_HINTS, emptyStringArray);

        Map<String, ArrayList<String>> hintMap = new HashMap<>();
        hintMap.put(PUZZLE_P1, hintList);
        hintMap.put(PUZZLE_P2, hintList);
        hintMap.put(PUZZLE_P3, hintList);
        puzzleMap.put(PUZZLE_HINT_MAP, hintMap);

        puzzleMap.put(PUZZLE_ORDER, order);
        puzzleMap.put(PUZZLE_PROMPT, "");
        puzzleMap.put(PUZZLE_TYPE, type);
        puzzleMap.put(PUZZLE_3D_OBJ_HEIGHTS, heights);
        puzzleMap.put(PUZZLE_CAN_TURN, canTurn);

        Map<String, ArrayList<String>> augImgUrlMap = new HashMap<>();
        augImgUrlMap.put(PUZZLE_P1, augImgUrlList);
        augImgUrlMap.put(PUZZLE_P2, emptyStringArray);
        augImgUrlMap.put(PUZZLE_P3, emptyStringArray);
        puzzleMap.put(PUZZLE_AUG_IMG_URL_MAP, augImgUrlMap);

        Map<String, ArrayList<String>> objUrlMap = new HashMap<>();
        objUrlMap.put(PUZZLE_P1, objUrlList);
        objUrlMap.put(PUZZLE_P2, emptyStringArray);
        objUrlMap.put(PUZZLE_P3, emptyStringArray);
        puzzleMap.put(PUZZLE_3D_OBJ_URL_MAP, objUrlMap);

        Map<String, ArrayList<Integer>> objHeightsMap = new HashMap<>();
        objHeightsMap.put(PUZZLE_P1, objHeigthsList);
        objHeightsMap.put(PUZZLE_P2, emptyIntegerArray);
        objHeightsMap.put(PUZZLE_P3, emptyIntegerArray);
        puzzleMap.put(PUZZLE_3D_OBJ_HEIGHTS_MAP, objHeightsMap);

        Map<String, ArrayList<Boolean>> canTurnMap = new HashMap<>();
        canTurnMap.put(PUZZLE_P1, canTurnList);
        canTurnMap.put(PUZZLE_P2, emptyBooleanArray);
        canTurnMap.put(PUZZLE_P3, emptyBooleanArray);
        puzzleMap.put(PUZZLE_CAN_TURN_MAP, canTurnMap);

        puzzleMap.put(PUZZLE_PROMPT_ARR, promptList);
        puzzleMap.put(PUZZLE_STORY, story);


        xpCol.document(uidQuest).collection("puzzles").document().set(puzzleMap);
    }

    public static void createXpQuest(double lat, double lon, String color, String description, int minPlayers, int nPuzzles, String title, int until, int xp){
        Map<String, Object> questMap = new HashMap<>();
        questMap.put(QUEST_LOCATION_FIELD, new GeoPoint(lat, lon));
        questMap.put(QUEST_COLOR_FIELD, color);
        questMap.put(QUEST_DESCRIPTION_FIELD, description);
        questMap.put(QUEST_MIN_PLAYERS, minPlayers);
        questMap.put(QUEST_NUM_PUZZLES, nPuzzles);
        questMap.put(QUEST_TITLE_FIELD, title);
        questMap.put(QUEST_UNTIL_FIELD, until);
        questMap.put(QUEST_XP_FIELD, xp);

        xpCol.add(questMap);
    }

    /**
     * @return The maximum allowed team size
     */
    public static int getMaxTeamSize() {
        return MAX_TEAM_SIZE;
    }

    /**
     * Creates a new user with the given username.
     * @param username Unique username that will be visible in game
     */
    public static void createUser(String username) {
        if (NameChecker.isNameCorrect(username)) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("name", username);
            userMap.put("xp", 0);
            userMap.put("team_ref", null);
            userMap.put("do_group_ref", null);
            userMap.put("study_association_ref", null);
            addSuccessListener(userCol.document(userID).set(userMap));
        } else {
            Log.w(TAG, "Try another username");
        }
    }

    /**
     * Deletes the user from Firestore. Also handles everything that is needed to
     * correctly leave the user's groups if any.
     */
    public static void deleteUser() {
        groupCount = -1; // because player is also counted in groups
        int currentCount = 0;
        for (Group group : groups) {
            if (!group.getUid().isEmpty()) {
                groupCount++;
            }
        }
        // groupCount needed because Google sign out needs to be called at the last moment and not before
        if (groupCount == 0) {
            deleteAccount();
        } else {
            if (!team.getUid().isEmpty()) {
                currentCount++;
                handleOwnershipAndLeave(teamCol, null, team.getUid(), false, isLastGroup(currentCount, groupCount));
            }
            if (!dogroup.getUid().isEmpty()) {
                currentCount++;
                handleOwnershipAndLeave(dogroupCol, null, dogroup.getUid(), false, isLastGroup(currentCount, groupCount));
            }
            if (!study.getUid().isEmpty()) {
                currentCount++;
                handleOwnershipAndLeave(studyCol, null, study.getUid(), false, isLastGroup(currentCount, groupCount));
            }
        }
    }

    /**
     * Function needed for account deletion procedure
     * @param currentCount Count of the amount of groups handled
     * @param groupCount The amount of groups the user is in
     * @return Returns true if the last group will now be handled
     */
    private static boolean isLastGroup(int currentCount, int groupCount) {
        return currentCount == groupCount;
    }

    /**
     * Changes the username, but first checks if it's correct.
     * @param newUsername New username
     * @return Returns true if the given name was correct.
     */
    public static boolean changeUsername(String newUsername) {
        if (NameChecker.isNameCorrect(newUsername)) {
            userCol.document(userID).update("name", newUsername).addOnSuccessListener(o -> getCollectionReferencesFromUser());
            return true;
        }
        return false;
    }

    /**
     * Increments the member count of a group if a user joins.
     * @param colRef Collection reference of group
     * @param uidGroup Unique identifier of group
     */
    private static void incrementMemberCount(CollectionReference colRef, String uidGroup) {
        colRef.document(uidGroup).update("member_count", increment(1));
    }

    /**
     * Creates a new group with the given group name.
     * Also handles ownership in group to leave if needed.
     * @param groupName   The unique group name
     * @param colRefGroup The type of collection: team, do-group or study association
     * @param uidGroupToLeave Unique identifier of group to leave
     */
    public static void createGroup(String groupName, CollectionReference colRefGroup, String uidGroupToLeave) {
        Map<String, Object> groupMap = new HashMap<>();
        groupMap.put("name", groupName);
        groupMap.put("xp", 0);
        groupMap.put("member_count", 0);
        if (colRefGroup.equals(teamCol)) {
            groupMap.put("discoverable", true);
        }
        // add() generates and returns a document ID automatically
        colRefGroup.add(groupMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String uidGroup = task.getResult().getId();
                addSuccessListener(colRefGroup.document(uidGroup).set(groupMap)); // setting group map
                if (!uidGroupToLeave.isEmpty()) { // if user is currently in another group
                    Log.i(TAG, "Currently in group");
                    handleOwnershipAndLeave(colRefGroup, uidGroup, uidGroupToLeave, true, false);
                } else {
                    setGroupToJoin(colRefGroup, uidGroup, true);
                }
            } else {
                Log.e(TAG, "Error creating group: " + task.getException());
            }
        });
    }

    /**
     * Creates a sub-collection of a group in which the members will be stored (first only the owner with role = 2).
     * It's only called when a new group is created.
     * @param colRefGroup The type of collection: team, do-group or study association
     * @param uidUser Unique identifier of user
     * @param uidGroup    Unique identifier of group
     * @param role The role the new member gets assigned
     */
    public static void createMember(CollectionReference colRefGroup, String uidUser, String uidGroup, int role) {
        Map<String, Object> memberMap = new HashMap<>();
        memberMap.put("role", role);
        addSuccessListener(db.collection(colRefGroup.getPath() + "/" + uidGroup + "/member").document(uidUser).set(memberMap));
        incrementMemberCount(colRefGroup, uidGroup);
    }

    /**
     * This function is called from the UI. From here all different kinds of sub functions are called.
     * It will eventually let the user join a group.
     * @param colRefGroup Collection reference of group
     * @param uidGroupToJoin Unique identifier of group to join
     * @param uidGroupToLeave Unique identifier of group to leave
     */
    public static void joinGroup(CollectionReference colRefGroup, String uidGroupToJoin, String uidGroupToLeave) {
        if (colRefGroup.equals(teamCol)) {
            colRefGroup.document(uidGroupToJoin).collection("member").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult().getDocuments().size() >= MAX_TEAM_SIZE) {
                        settingsFragment.loadSettings();
                        mainActivity.toast("Team is full (3/3)");
                    } else {
                        joinGroup2(colRefGroup, uidGroupToJoin, uidGroupToLeave);
                    }
                }
            });
        } else {
            joinGroup2(colRefGroup, uidGroupToJoin, uidGroupToLeave);
        }

    }

    /**
     * Only handles ownership if the user has that type of group.
     * Otherwise, the user can directly join it.
     * @param colRefGroup Collection reference of group
     * @param uidGroupToJoin Unique identifier of group to join
     * @param uidGroupToLeave Unique identifier of group to leave
     */
    private static void joinGroup2(CollectionReference colRefGroup, String uidGroupToJoin, String uidGroupToLeave) {
        if (!uidGroupToLeave.isEmpty()) {
            Log.i(TAG, "Leaving group and handling ownership");
            handleOwnershipAndLeave(colRefGroup, uidGroupToJoin, uidGroupToLeave, false, false); // leave group properly
        } else {
            setGroupToJoin(colRefGroup, uidGroupToJoin, false);
        }
    }

    /**
     * Creates the user document in the 'member' sub collection and gives the user the owner role
     * if it's a newly created group. Otherwise just the default role.
     * @param colRefGroup Collection reference of group
     * @param uidGroupToJoin Unique identifier of group to join
     * @param newGroup If user created a new team and joins it
     */
    private static void setGroupToJoin(CollectionReference colRefGroup, String uidGroupToJoin, boolean newGroup) {
        updateGroupReferenceUser(colRefGroup, uidGroupToJoin);

        if (newGroup) {
            createMember(colRefGroup, userID, uidGroupToJoin, OWNER_ROLE);
        } else {
            createMember(colRefGroup, userID, uidGroupToJoin, DEFAULT_ROLE);
        }
        Log.i(TAG, "Joined new group");
    }

    /**
     * Handles the ownership in a group when the user leaves. If the user is the last one in a group and leaves,
     * the group is deleted. If the user is owner and there are still other members, ownership is transferred.
     * In either cases, the user document will eventually be deleted from the 'member' sub collection.
     * @param colRefGroup Collection reference of group
     * @param uidGroupToJoin Unique identifier of group to join
     * @param uidGroupToLeave Unique identifier of group to leave
     * @param newGroup If user created a new team and joins it
     * @param isLastGroup If it's the last group that is being handled for ownership (only necessary for account deletion)
     */
    private static void handleOwnershipAndLeave(CollectionReference colRefGroup, String uidGroupToJoin, String uidGroupToLeave, boolean newGroup, boolean isLastGroup) {
        colRefGroup.document(uidGroupToLeave).collection("member").get().addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                List<DocumentSnapshot> memberListGroupToLeave = task1.getResult().getDocuments(); // all member documents from group to leave
                int memberAmountGroupToLeave = memberListGroupToLeave.size();
                Log.i(TAG, "MEMBERS: " + memberAmountGroupToLeave);

                // if user is the last member
                if (memberAmountGroupToLeave == 1) {
                    Log.i(TAG, "Member = 1");
                    if (colRefGroup.equals(teamCol)) {
                        Log.i(TAG, "Deleting group");
                        deleteUserFromMember(colRefGroup, uidGroupToLeave, true, OWNER_ROLE, isLastGroup); // delete group to leave, because it's a team
                    } else {
                        deleteUserFromMember(colRefGroup, uidGroupToLeave, false, OWNER_ROLE, isLastGroup); // don't delete group, because it's not a team
                    }
                } else {
                    // if there are more members
                    for (int i = 0; i < memberAmountGroupToLeave; i++) {
                        DocumentSnapshot memberDoc = memberListGroupToLeave.get(i);
                        // find user document
                        if (memberDoc.getId().equals(userID) && memberDoc.getLong("role").intValue() == OWNER_ROLE) {
                            Log.i(TAG, "User " + i + " is owner");
                            // pass ownership to the next or previous group member
                            if (i == 0) {
                                changeUserRole(colRefGroup, uidGroupToLeave, memberListGroupToLeave.get(i + 1), OWNER_ROLE, OWNER_ROLE);
                            } else {
                                changeUserRole(colRefGroup, uidGroupToLeave, memberListGroupToLeave.get(i - 1), OWNER_ROLE, OWNER_ROLE);
                            }
                            break;
                        }
                    }
                    deleteUserFromMember(colRefGroup, uidGroupToLeave, false, DEFAULT_ROLE, isLastGroup); // remove user reference from group
                }

                // if null, this method is called from deleteUser()
                if (uidGroupToJoin != null) {
                    // actually join group after handling ownership
                    setGroupToJoin(colRefGroup, uidGroupToJoin, newGroup);
                }
            } else {
                Log.e(TAG, "Failure");
            }
        });
    }

    /**
     * Deletes user from 'member' sub collection
     * @param colRefGroup Collection reference of group
     * @param uidGroup Unique identifier of group
     * @param deleteGroup Whether to delete the entire group
     * @param role The user's role
     * @param isLastGroup If it's the last group that is being handled for ownership (only necessary for account deletion)
     */
    private static void deleteUserFromMember(CollectionReference colRefGroup, String uidGroup, boolean deleteGroup, int role, boolean isLastGroup) {
        Log.i(TAG, "Deleting member");
        // decrement member_count
        colRefGroup.document(uidGroup).update("member_count", increment(-1)).addOnCompleteListener(task2 -> {
            if(task2.isSuccessful()) {
                colRefGroup.document(uidGroup).collection("member").document(userID).delete().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (deleteGroup && isValidRole(role) && role == OWNER_ROLE) {
                            // sub collection must be deleted first, otherwise there will be deletion problems
                            colRefGroup.document(uidGroup).delete();
                        }

                        // if it's the last group that is being handled for ownership etc. due to account deletion
                        if (isLastGroup) {
                            groupCount = 0;
                            deleteAccount();
                        }
                    }
                });
            }
        });
    }

    /**
     * Deletes Firestore account first, then Google account from Authentication
     */
    private static void deleteAccount() {
        // used for account deletion method
        userCol.document(userID).delete().addOnCompleteListener(task1 -> {
            // delete Google account from Firebase
            GoogleLogin.deleteAccount();
        });
    }

    /**
     * Changes whether the group is private or public
     * @param uidTeam Unique identifier of team
     * @param value Private (false), public (true)
     */
    public static void changeDiscoverabilityTeam(String uidTeam, boolean value) {
        teamCol.document(uidTeam).update("discoverable", value).addOnSuccessListener(o -> {getCollectionReferencesFromUser();});
    }

    /**
     * Updates the player's xp after finishing a quest.
     * Also adds xp to the player's groups if there are any.
     * @param xpToAdd The xp to add
     */
    public static void updateXp(int xpToAdd) {
        userCol.document(userID).update("xp", increment(xpToAdd));
        if (!team.getUid().isEmpty()) {
            teamCol.document(team.getUid()).update("xp", increment(xpToAdd));
        }
        if (!dogroup.getUid().isEmpty()) {
            dogroupCol.document(dogroup.getUid()).update("xp", increment(xpToAdd));
        }
        if (!study.getUid().isEmpty()) {
            studyCol.document(study.getUid()).update("xp", increment(xpToAdd));
        }
    }

    /**
     * Changes the group name
     * @param colRefGroup Collection reference of group
     * @param uidGroup Unique identifier of group
     * @param newGroupName The new group name
     * @return Returns true if the given name was correct
     */
    public static boolean changeGroupName(CollectionReference colRefGroup, String uidGroup, String newGroupName) {
        if (NameChecker.isNameCorrect(newGroupName)) {
            colRefGroup.document(uidGroup).update("name", newGroupName).addOnSuccessListener(o -> getCollectionReferencesFromUser());
            return true;
        }
        return false;
    }

    /**
     * Function that checks whether the user is authorised to change someone else's role.
     * If the user passes the check, then the new role is assigned accordingly.
     * @param colRefGroup Collection reference of group
     * @param uidGroup Unique identifier of group
     * @param otherUser The other user who gets the new role
     * @param assignerRole Role of the assigner
     * @param newRole The new role
     */
    private static void changeUserRole(CollectionReference colRefGroup, String uidGroup, DocumentSnapshot otherUser, int assignerRole, int newRole) {
        Log.d(TAG, "Changing role...");
        if (!isValidRole(assignerRole) || !isValidRole(newRole)) {
            Log.e(TAG, "Role " + newRole + " is not allowed. Correct roles: 0 (normal), 1 (admin) or 2 (owner)");
        } else if (assignerRole == 0) {
            Log.w(TAG, "User not authorised to change role");
        } else if (newRole > assignerRole) {
            Log.w(TAG, "New role higher than assigner's role is not allowed");
        } else {
            String uidOtherUser = otherUser.getId();
            int roleOtherUser = otherUser.getLong("role").intValue();
            if (roleOtherUser == OWNER_ROLE) {
                Log.w(TAG, "Can't change the owner's role");
            } else if (roleOtherUser > assignerRole) { // will always be false for now, but is needed if there would be more than 3 roles
                Log.w(TAG, "User not authorised to change role");
            } else {
                Log.d(TAG, "Giving " + uidOtherUser + " new role " + newRole + " in group " + uidGroup);
                addSuccessListener(db.collection(colRefGroup.getPath() + "/" + uidGroup + "/member").document(uidOtherUser).update("role", newRole));
            }
        }
    }

    /**
     * Checks if the role is 0, 1 or 2
     * @param role Role
     * @return Returns true if the role represented by an integer is correct
     */
    private static boolean isValidRole(int role) {
        return role >= 0 && role <= OWNER_ROLE;
    }

    private static MainActivity mainActivity;
    private static QuestsFragment questsFragment;
    private static MapFragment mapFragment;
    private static LeaderboardsFragment leaderboardsFragment;
    private static ProfileFragment profileFragment;
    private static SettingsFragment settingsFragment;
    private static PlayFragment playFragment;

    /**
     * Sets all the fragments
     * @param main Main activity that contains all the fragments
     */
    public static void setFragments(MainActivity main) {
        mainActivity = main;
        questsFragment = main.getQuestsFragment();
        mapFragment = main.getMapFragment();
        leaderboardsFragment = main.getLeaderboardsFragment();
        profileFragment = main.getProfileFragment();
        settingsFragment = main.getSettingsFragment();
        playFragment = main.getPlayFragment();
    }

    private static int questTypesLoaded;
    private static int puzzlesLoaded;

    /**
     * Thread-safe implementation to make sure all quests and corresponding puzzles are loaded
     * before the application continues
     */
    private synchronized static void sendQuests() {
        if (questTypesLoaded < 1) {
            questTypesLoaded += 1;
            return;
        }

        int puzzlesToLoad = 0;
        for (Quest q : quests) {
            puzzlesToLoad = puzzlesToLoad + q.getnPuzzles();
        }

        if (puzzlesLoaded < puzzlesToLoad) {
            puzzlesLoaded += 1;
            return;
        }

        for (Quest q : quests) {
            q.getPuzzles().sort(new Comparator<Puzzle>() {
                @Override
                public int compare(Puzzle puzzle, Puzzle t1) {
                    return puzzle.getOrder() > t1.getOrder() ? 1 : -1;
                }
            });
        }
        questsFragment.stopRefreshing();
        questsFragment.loadAllTabs();
        mapFragment.loadMarkers();
    }

    /**
     * Transforms the raw augmented image map received from the database
     *
     * @param raw - what is received from db.
     * @return - more usable format of augmented image map, to be used in Puzzle
     */
    private static Map<Integer, List<Pair<String, String>>> transformAugMap(Map<String, List<String>> raw) {
        Map<String, Integer> playerMap = new HashMap<>();
        playerMap.put(PUZZLE_P1, 0);
        playerMap.put(PUZZLE_P2, 1);
        playerMap.put(PUZZLE_P3, 2);

        Map<Integer, List<Pair<String, String>>> res = new HashMap<>();
        for (String player : raw.keySet()) {
            List<String> entry = raw.get(player);
            List<Pair<String, String>> playerSet = new ArrayList<>();
            for (int i = 0; i < entry.size() - 1; i += 2) {
                Pair<String, String> augImgPair =
                        new Pair<>(entry.get(i), entry.get(i + 1));
                playerSet.add(augImgPair);
            }
            res.put(playerMap.get(player), playerSet);
        }
        return res;
    }

    /**
     * Transforms the raw hint map received from the database
     *
     * @param raw - what is received from db
     * @return - more usable format, mapping place of player in team to hints
     */
    private static Map<Integer, List<String>> transformStringMap(Map<String, List<String>> raw) {
        Map<String, Integer> playerMap = new HashMap<>();
        playerMap.put(PUZZLE_P1, 0);
        playerMap.put(PUZZLE_P2, 1);
        playerMap.put(PUZZLE_P3, 2);

        Map<Integer, List<String>> res = new HashMap<>();
        for (String player : raw.keySet()) {
            res.put(playerMap.get(player), raw.get(player));
        }
        return res;
    }

    private static Map<Integer, List<Long>> transformNumberMap(Map<String, List<Long>> raw) {
        Map<String, Integer> playerMap = new HashMap<>();
        playerMap.put(PUZZLE_P1, 0);
        playerMap.put(PUZZLE_P2, 1);
        playerMap.put(PUZZLE_P3, 2);

        Map<Integer, List<Long>> res = new HashMap<>();
        for(String player : raw.keySet()) {
            res.put(playerMap.get(player), raw.get(player));
        }
        return res;
    }

    private static Map<Integer, List<Boolean>> transformBoolMap(Map<String, List<Boolean>> raw) {
        Map<String, Integer> playerMap = new HashMap<>();
        playerMap.put(PUZZLE_P1, 0);
        playerMap.put(PUZZLE_P2, 1);
        playerMap.put(PUZZLE_P3, 2);

        Map<Integer, List<Boolean>> res = new HashMap<>();
        for(String player : raw.keySet()) {
            res.put(playerMap.get(player), raw.get(player));
        }
        return res;
    }

    /**
     * Gets all quests from the database and puts it into protected static class variables
     * this method should be called on starting the app and when refreshing the quests page
     * WARNING: THIS IS JAAP CODE.
     *
     * @return a list with all quests parsed into Quest objects
     */
    public static List<Quest> getAllQuests() {
        //pre
        typeMap.put(L_BASED, PUZZLETYPE.LOCATION_BASED);
        typeMap.put(A_IMAGE, PUZZLETYPE.AUGMENTED_IMAGE);
        typeMap.put(REG, PUZZLETYPE.REGULAR);
        //
        quests.clear();
        rewardQuests.clear();
        xpQuests.clear();
        questTypesLoaded = 0;
        //processing reward quests
        rewardCol.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int nQuests = task.getResult().getDocuments().size();
                Log.i(TAG, nQuests + " quests found in database");
                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                    GeoPoint l = doc.getGeoPoint(QUEST_LOCATION_FIELD);
                    RewardQuest temp = new RewardQuest(mainActivity);
                    temp.setRewardDescription(doc.getString(QUEST_REWARD_FIELD))
                            .setTitle(doc.getString(QUEST_TITLE_FIELD))
                            .setDescription(doc.getString(QUEST_DESCRIPTION_FIELD))
                            .setColor(doc.getString(QUEST_COLOR_FIELD))
                            .setUntil(doc.getLong(QUEST_UNTIL_FIELD))
                            .setLocation(new LatLng(l.getLatitude(), l.getLongitude()))
                            .setnPuzzles(doc.getLong(QUEST_NUM_PUZZLES).intValue())
                            .setMinimumPlayers(doc.getLong(QUEST_MIN_PLAYERS).intValue());

                    //getting the puzzles
                    CollectionReference puzzles = doc.getReference().collection("puzzles");
                    puzzles.get().addOnCompleteListener(pTask -> {
                        if (pTask.isSuccessful()) {
                            //puzzle get success
                            for (DocumentSnapshot pDoc : pTask.getResult().getDocuments()) {
                                GeoPoint pLoc = pDoc.getGeoPoint(PUZZLE_LOC);
                                Puzzle tempPuzzle = new Puzzle(temp);
                                LatLng pLocg = new LatLng(pLoc.getLatitude(), pLoc.getLongitude());
                                //get the puzzle type
                                Long intType = pDoc.getLong(PUZZLE_TYPE);
                                PUZZLETYPE t = typeMap.get(intType);
                                //get the augmented image pairs
                                /*List<Pair<String, String>> augTemp = new ArrayList<>();
                                List<String> raw = (List<String>) pDoc.get(PUZZLE_AUG_IMG_URL);
                                for (int i=0;i<6;i=i+2) {
                                    augTemp.add(new Pair<String, String>(raw.get(i), raw.get(i+1)));
                                }*/
                                List<GeoPoint> locArr = (List<GeoPoint>) pDoc.get(PUZZLE_LOC_ARR);
                                //get the augmented image map
                                Map<String, List<String>> rawAugMap = (Map<String, List<String>>) pDoc.get(PUZZLE_AUG_IMG_URL_MAP);

                                //get the 3d objects
                                Map<Integer, List<AR3DObject>> ar3DObjects = new HashMap<>();
                                Map<Integer, List<String>> objectUrls =
                                        (Map<Integer, List<String>>) transformStringMap((Map<String, List<String>>) pDoc.get(PUZZLE_3D_OBJ_URL_MAP));
                                Map<Integer, List<Long>> objectHeights =
                                        (Map<Integer, List<Long>>) transformNumberMap((Map<String, List<Long>>) pDoc.get(PUZZLE_3D_OBJ_HEIGHTS_MAP));
                                Map<Integer, List<Boolean>> objectCanTurns =
                                        (Map<Integer, List<Boolean>>) transformBoolMap((Map<String, List<Boolean>>) pDoc.get(PUZZLE_CAN_TURN_MAP));
                                for (Integer i : objectUrls.keySet()) {
                                    List<AR3DObject> temparobjlist = new ArrayList<>();
                                    List<String> playerObjectUrl = objectUrls.get(i);
                                    for (int j = 0; j < playerObjectUrl.size(); j++) {
                                        LatLng location;
                                        if (locArr == null || locArr.get(0) == null) {
                                            location = pLocg;
                                        } else {
                                            location = new LatLng(locArr.get(j).getLatitude(), locArr.get(j).getLongitude());
                                        }
                                        AR3DObject temparobj = new AR3DObject(
                                                location,
                                                playerObjectUrl.get(j),
                                                objectHeights.get(i).get(j).intValue(),
                                                objectCanTurns.get(i).get(j)
                                        );
                                        temparobjlist.add(temparobj);
                                    }
                                    ar3DObjects.put(i, temparobjlist);
                                }

                                tempPuzzle.setType(t)
                                        .setLocation(pLocg)
                                        .setLocationArray(locArr)
                                        .setCorrectAnswer(pDoc.getString(PUZZLE_ANSWER))
                                        .setPrompt(pDoc.getString(PUZZLE_PROMPT))
                                        .setPrompts((List<String>) pDoc.get(PUZZLE_PROMPT_ARR))
                                        /*setAugImage(augTemp)*/
                                        .setAugmentedImageMap(transformAugMap(rawAugMap))
                                        .setStory(pDoc.getString(PUZZLE_STORY))
                                        /*.setAR3DObjects(ar3DObjects)*/
                                        .setAR3DObjectsMap(ar3DObjects)
                                        .setHintMap(transformStringMap((Map<String, List<String>>) pDoc.get(PUZZLE_HINT_MAP)))
                                        /*.setHints((List<String>) pDoc.get(PUZZLE_HINTS))*/
                                        .setOrder(pDoc.getLong(PUZZLE_ORDER).intValue());
                                temp.addPuzzle(tempPuzzle);

                                sendQuests();
                            }
                        }
                    });
                    /////////////////////////

                    quests.add(temp);
                    rewardQuests.add(temp);
                }
                Log.i(TAG, "successfully fetched all reward quests");
            } else {
                Log.w(TAG, "Failure getting reward quests");
            }
            sendQuests();
        });

        //processing xp quests.
        xpCol.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int nQuests = task.getResult().getDocuments().size();
                Log.i(TAG, nQuests + " quests found in database");
                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                    GeoPoint l = doc.getGeoPoint(QUEST_LOCATION_FIELD);
                    XpQuest temp = new XpQuest(mainActivity);
                    temp.setXp(doc.getLong(QUEST_XP_FIELD).intValue())
                            .setTitle(doc.getString(QUEST_TITLE_FIELD))
                            .setDescription(doc.getString(QUEST_DESCRIPTION_FIELD))
                            .setColor(doc.getString(QUEST_COLOR_FIELD))
                            .setUntil(doc.getLong(QUEST_UNTIL_FIELD))
                            .setLocation(new LatLng(l.getLatitude(), l.getLongitude()))
                            .setnPuzzles(doc.getLong(QUEST_NUM_PUZZLES).intValue())
                            .setMinimumPlayers(doc.getLong(QUEST_MIN_PLAYERS).intValue());

                    //getting the puzzles
                    CollectionReference puzzles = doc.getReference().collection("puzzles");
                    puzzles.get().addOnCompleteListener(pTask -> {
                        if (pTask.isSuccessful()) {
                            //puzzle get success
                            for (DocumentSnapshot pDoc : pTask.getResult().getDocuments()) {
                                GeoPoint pLoc = pDoc.getGeoPoint(PUZZLE_LOC);
                                Puzzle tempPuzzle = new Puzzle(temp);
                                LatLng pLocg =null;
                                if (pLoc != null) {
                                     pLocg = new LatLng(pLoc.getLatitude(), pLoc.getLongitude());
                                }
                                //get the puzzle type
                                Long intType = pDoc.getLong(PUZZLE_TYPE);
                                PUZZLETYPE t = typeMap.get(intType);
                                //get the augmented image pairs
                                /*
                                List<Pair<String, String>> augTemp = new ArrayList<>();
                                List<String> raw = (List<String>) pDoc.get(PUZZLE_AUG_IMG_URL);
                                for (int i=0;i<6;i=i+2) {
                                    augTemp.add(new Pair<String, String>(raw.get(i), raw.get(i+1)));
                                }*/
                                List<GeoPoint> locArr = (List<GeoPoint>) pDoc.get(PUZZLE_LOC_ARR);

                                //get the augmented image map
                                Map<String, List<String>> rawAugMap = (Map<String, List<String>>) pDoc.get(PUZZLE_AUG_IMG_URL_MAP);

                                //get the 3d objects
                                Map<Integer, List<AR3DObject>> ar3DObjects = new HashMap<>();
                                Map<Integer, List<String>> objectUrls =
                                        (Map<Integer, List<String>>) transformStringMap((Map<String, List<String>>) pDoc.get(PUZZLE_3D_OBJ_URL_MAP));
                                Map<Integer, List<Long>> objectHeights =
                                        (Map<Integer, List<Long>>) transformNumberMap((Map<String, List<Long>>) pDoc.get(PUZZLE_3D_OBJ_HEIGHTS_MAP));
                                Map<Integer, List<Boolean>> objectCanTurns =
                                        (Map<Integer, List<Boolean>>) transformBoolMap((Map<String, List<Boolean>>) pDoc.get(PUZZLE_CAN_TURN_MAP));
                                for (Integer i : objectUrls.keySet()) {
                                    List<AR3DObject> temparobjlist = new ArrayList<>();
                                    List<String> playerObjectUrl = objectUrls.get(i);
                                    for (int j = 0; j < playerObjectUrl.size(); j++) {
                                        LatLng location;
                                        if (locArr == null || locArr.get(0) == null) { //kut code
                                            location = pLocg;
                                        } else {
                                            location = new LatLng(locArr.get(j).getLatitude(), locArr.get(j).getLongitude());                                        }
                                        AR3DObject temparobj = new AR3DObject(
                                                location,
                                                playerObjectUrl.get(j),
                                                objectHeights.get(i).get(j).intValue(),
                                                objectCanTurns.get(i).get(j)
                                        );
                                        temparobjlist.add(temparobj);
                                    }
                                    ar3DObjects.put(i, temparobjlist);
                                }

                                tempPuzzle.setType(t)
                                        .setLocation(pLocg)
                                        .setLocationArray(locArr)
                                        .setCorrectAnswer(pDoc.getString(PUZZLE_ANSWER))
                                        .setPrompt(pDoc.getString(PUZZLE_PROMPT))
                                        .setPrompts((List<String>) pDoc.get(PUZZLE_PROMPT_ARR))
                                        .setStory(pDoc.getString(PUZZLE_STORY))
                                        //.setAugImage(augTemp)
                                        .setAugmentedImageMap(transformAugMap(rawAugMap))
                                        .setAR3DObjectsMap(ar3DObjects)
                                        /*.setAR3DObjects(ar3DObjects)*/
                                        /*.setHints((List<String>) pDoc.get(PUZZLE_HINTS))*/
                                        .setHintMap(transformStringMap((Map<String, List<String>>) pDoc.get(PUZZLE_HINT_MAP)))
                                        .setOrder(pDoc.getLong(PUZZLE_ORDER).intValue());
                                temp.addPuzzle(tempPuzzle);

                                sendQuests();
                            }
                        }
                    });

                    //
                    quests.add(temp);
                    xpQuests.add(temp);
                }
                Log.i(TAG, "successfully fetched all xp quests");
            } else {
                Log.w(TAG, "Failure getting reward quests");
            }
            sendQuests();
        });

        return quests;
    }

    /**
     * Gets all initial data needed from Firebase including leaderboards and profile data
     */
    public static void getAllData() {
        // load the quests for the quests page
        getAllQuests();
        // load the leaderboards and the profile cards
        getCollectionReferencesFromUser();
    }

    /**
     * Gets document references to all groups the user is in and puts them in an array
     */
    public static void getCollectionReferencesFromUser() {
        userCol.document(userID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // these do not refer to the top directory collections, but to for example 'team/testTeam/member/testUser'
                DocumentReference[] docRefArray = new DocumentReference[]{
                        (DocumentReference) task.getResult().get("team_ref"),
                        (DocumentReference) task.getResult().get("do_group_ref"),
                        (DocumentReference) task.getResult().get("study_association_ref")};
                getLeaderboardUser(docRefArray);
            } else {
                Log.e(TAG, "Failure getting user");
            }
        });
    }

    /**
     * Adds the player card in profile. When finished, it calls getLeaderboardGroup to add the group cards.
     *
     * @param docRefArray An array with all the document references of the user. Index 0 refers to 'team', 1 to 'do-group' and 2 to 'study association'.
     */
    private static void getLeaderboardUser(DocumentReference[] docRefArray) {
        groups.clear();
        userCol.orderBy("xp", Query.Direction.DESCENDING).get().addOnCompleteListener(task -> {
            List<Object[]> leaderboard = new ArrayList<>();
            for (int i = 0; i < task.getResult().getDocuments().size(); i++) {
                DocumentSnapshot docSnap = task.getResult().getDocuments().get(i);
                int place = i + 1;
                int score = docSnap.getLong("xp").intValue();
                String name = docSnap.get("name").toString();
                if (docSnap.getId().equals(userID)) {
                    //Log.d(TAG, "[" + place + "] " + name + " (YOU) - " + score);
                    player = new Player(name, place, score, userID, "Player");
                    groups.add(player);
                    getLeaderboardGroup(docRefArray); // this method is called here, because of the order of profile cards (first user then groups)
                    leaderboard.add(new Object[]{name, score, true});
                } else {
                    leaderboard.add(new Object[]{name, score, false});
                }
            }
            leaderboardPlayers = leaderboard;
        });
    }

    private static int groupsLoaded;

    /**
     * Loads profile fragment if all the groups are loaded
     */
    private synchronized static void sendGroups() {
        if (groupsLoaded < 2) {
            groupsLoaded += 1;
            return;
        }
        if (profileFragment != null) {
            profileFragment.loadCards();
            profileFragment.stopRefreshing();
        }
        if (settingsFragment != null) sendSettings();
    }

    private static int leaderboardsLoaded;

    /**
     * Loads leaderboards fragment if all the leaderboards are loaded
     */
    private synchronized static void sendLeaderboards() {
        if (leaderboardsLoaded < 2) {
            leaderboardsLoaded += 1;
            return;
        }
        if (leaderboardsFragment != null) {
            leaderboardsFragment.loadAllTabs();
            leaderboardsFragment.stopRefreshing();
        }
        if (settingsFragment != null) sendSettings();
    }

    // settings are only send when leaderboards and groups are already loaded
    private static int groupsOrLeaderboardsSent;

    /**
     * Loads the settings if all the groups and all the leaderboards are loaded
     */
    private synchronized static void sendSettings() {
        if (groupsOrLeaderboardsSent < 1) {
            groupsOrLeaderboardsSent += 1;
            return;
        }
        settingsFragment.loadSettings();
        settingsFragment.stopRefreshing();
    }

    /**
     * Gets the leaderboard for every type of group. While doing this, this method also adds the profile cards,
     * because it requires the same query.
     *
     * @param docRefArray An array with all the document references of the user. Index 0 refers to 'team', 1 to 'do-group' and 2 to 'study association'.
     */
    private static void getLeaderboardGroup(DocumentReference[] docRefArray) {
        groupsLoaded = 0;
        leaderboardsLoaded = 0;
        groupsOrLeaderboardsSent = 0;
        // do this for every group
        for (int i = 0; i < docRefArray.length; i++) {
            DocumentReference docRef = docRefArray[i];
            CollectionReference colRef = null;
            String uidGroup = "";
            // if user has no group
            if (docRef == null) {
                addGroupCardInProfile(i, "", -1, -1, DEFAULT_ROLE, uidGroup, false, 0, null); // null card has empty strings and -1 values
                switch (i) {
                    case 0:
                        colRef = teamCol;
                        break;
                    case 1:
                        colRef = dogroupCol;
                        break;
                    case 2:
                        colRef = studyCol;
                        break;
                    default:
                        Log.e(TAG, "Group type does not exist.");
                }
            } else {
                colRef = docRef.getParent().getParent().getParent();
                uidGroup = docRef.getParent().getParent().getId();
            }

            orderCollection(colRef, uidGroup, i);
        }
    }

    /**
     * Orders a group collection by xp. Also manages to retrieve amount of members and
     * the member names if it's a group collection
     * @param colRef Collection reference of group
     * @param uidGroup Unique identifier of group
     * @param type Group type 0, 1 or 2
     */
    private static void orderCollection(CollectionReference colRef, String uidGroup, int type) {
        // entire collection sorted by xp
        colRef.orderBy("xp", Query.Direction.DESCENDING).get().addOnCompleteListener(task -> {
            List<Object[]> leaderboard = new ArrayList<>();
            List<Object[]> settingsList = new ArrayList<>();

            boolean isTeamCol = type == 0;
            int groupCount = task.getResult().getDocuments().size(); // amount of groups in collection
            boolean userHasGroup = !uidGroup.isEmpty(); // if uidGroup is "", user has no group
            HashMap<String, Object> userStats = new HashMap<>();

            for (int k = 0; k < groupCount; k++) { // for every group in collection
                DocumentSnapshot docSnap = task.getResult().getDocuments().get(k);
                String docId = docSnap.getId();
                String name = docSnap.get("name").toString();
                int place = k + 1;
                int score = docSnap.getLong("xp").intValue();
                int memberCount = docSnap.getLong("member_count").intValue();

                boolean isUserGroup = docId.equals(uidGroup); // needed to mark the user in leaderboards
                leaderboard.add(new Object[]{name, score, isUserGroup});

                boolean isDiscoverable;

                if (!isTeamCol) { // not a team
                    isDiscoverable = true; // other group types are always discoverable
                    addToSettingsList(isDiscoverable, settingsList, docId, name, isUserGroup, false, memberCount);
                    if(isUserGroup) { // if user, add stats to profile
                        addGroupCardInProfile(type, name, place, score, DEFAULT_ROLE, docId, isDiscoverable, memberCount, null);
                    }
                    if (k == groupCount - 1) { // if last iteration, add leaderboard and settings
                        settingsList.sort((objects, t1) -> objects[1].toString().compareTo(t1[1].toString()));
                        addLeaderboard(leaderboard, settingsList, type);
                    }
                } else { // if team
                    isDiscoverable = docSnap.getBoolean("discoverable");

                    if(!isUserGroup) { // user team is added after for loop
                        addToSettingsList(isDiscoverable, settingsList, docId, name, false, true, memberCount);
                    } else { // save user stats if it's the user's team
                        userStats.put("id", docId);
                        userStats.put("name", name);
                        userStats.put("place", place);
                        userStats.put("score", score);
                        userStats.put("memberCount", memberCount);
                        userStats.put("discoverable", isDiscoverable);
                    }
                }
            }

            // only relevant if it's a team collection

            if(isTeamCol && userHasGroup) { // last thing to do if team, getting members
                getMembersTeam(type, userStats.get("name").toString(), (Integer) userStats.get("place"), (Integer) userStats.get("score"), uidGroup,
                        (Boolean) userStats.get("discoverable"), (Integer) userStats.get("memberCount"), settingsList, leaderboard);
            } else if(isTeamCol && !userHasGroup) { // if user doesn't have a team, don't get members
                settingsList.sort((objects, t1) -> objects[1].toString().compareTo(t1[1].toString()));
                addLeaderboard(leaderboard, settingsList, type);
            }
        });
    }

    /**
     * Gets the members of a team to display their names in profile.
     * Eventually adds the team 'card' in profile
     * @param type Group type 0, 1 or 2
     * @param name Team name
     * @param place Team placement in leaderboard
     * @param score Team score (xp)
     * @param uidGroup Unique identifier of group
     * @param isDiscoverable If team is discoverable
     * @param memberCount Amount of members in team
     * @param settingsList SettingsList
     * @param leaderboard Leaderboard
     */
    private static void getMembersTeam(int type, String name, int place, int score, String uidGroup, boolean isDiscoverable, int memberCount, List<Object[]> settingsList, List<Object[]> leaderboard) {
        teamCol.document(uidGroup).collection("member").get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                List<String> memberUidList = new ArrayList<>();
                int role = 0;
                for (DocumentSnapshot docSnap : task.getResult().getDocuments()) {
                    memberUidList.add(docSnap.getId());
                    if(docSnap.getId().equals(userID)) { // if user document in "member", retrieve role
                        role = docSnap.getLong("role").intValue();
                    }
                }
                // get all user names of team members
                int finalRole = role;
                userCol.whereIn(documentId(), memberUidList).get().addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful()) {
                        List<String> memberNameList = new ArrayList<>();
                        for (DocumentSnapshot docSnap : task2.getResult().getDocuments()) {
                            memberNameList.add(docSnap.get("name").toString());
                        }
                        addToSettingsList(isDiscoverable, settingsList, uidGroup, name, true, true, memberCount);
                        settingsList.sort((objects, t1) -> objects[1].toString().compareTo(t1[1].toString()));
                        // finally add team card to profile
                        addGroupCardInProfile(type, name, place, score, finalRole, uidGroup, isDiscoverable, memberCount, memberNameList);
                        addLeaderboard(leaderboard, settingsList, type);
                    }
                });
            }
        });
    }

    /**
     * Adds groups to settingsList. Depending on the type of group or if it's
     * the user's group, different Strings will be added.
     * @param isDiscoverable If the group is discoverable
     * @param settingsList SettingsList
     * @param docId group ID
     * @param name Group name
     * @param isUserGroup If it's the user's group
     * @param isTeamCol If it's a team collection
     * @param memberCount The member count of group
     */
    private static void addToSettingsList(boolean isDiscoverable, List<Object[]> settingsList, String docId, String name, boolean isUserGroup, boolean isTeamCol, int memberCount) {
        String str = name;
        if(isUserGroup) {
            str += " (current)";
            settingsList.add(new Object[]{docId, str});
        } else {
            if (isDiscoverable) {
                if (isTeamCol && memberCount != MAX_TEAM_SIZE) {
                    str += " (" + memberCount + "/" + MAX_TEAM_SIZE + ")";
                    settingsList.add(new Object[]{docId, str});
                } else if(!isTeamCol){
                    settingsList.add(new Object[]{docId, str});
                }
            }
        }
    }

    private static List<Object[]> listTeam;
    private static List<Object[]> listDoGroup;
    private static List<Object[]> listStudy;

    /**
     * Gets a settings list based on a query
     * @param query Enum that is related to a settings list
     * @return One of the settings lists
     */
    public static List<Object[]> getSettingsList(SettingsContent.Query query) {
        switch (query) {
            case TEAM:
                return listTeam;
            case DOGROUP:
                return listDoGroup;
            case STUDY:
                return listStudy;
            default:
                return null;
        }
    }

    /**
     * Saves the generated leadboard until all groups have been processed.
     * @param leaderboard Leaderboard
     * @param settingsList SettingsList
     * @param type Type of group 0, 1 or 2
     */
    private static void addLeaderboard(List<Object[]> leaderboard, List<Object[]> settingsList, int type) {
        switch (type) {
            case 0:
                leaderboardTeams = leaderboard;
                listTeam = settingsList;
                break;
            case 1:
                leaderboardDogroups = leaderboard; // TODO should only display dogroups of the study ass you're in
                listDoGroup = settingsList;
                break;
            case 2:
                leaderboardAssociations = leaderboard;
                listStudy = settingsList;
                break;
            default:
                Log.e(TAG, "Type does not exist.");
        }
        sendLeaderboards();
    }

    /**
     * Adds group card to profile, but waits until all groups have been processed to remain the order.
     * @param type  A number that refers to the type of group
     * @param name  Group name
     * @param place Group place
     * @param xp    Group xp
     * @param uid   Group uid
     * @param isDiscoverable If group is discoverable
     * @param memberCount Amount of members in group
     * @param members Members by name in a list
     */
    private static void addGroupCardInProfile(int type, String name, int place, int xp, int role, String uid, boolean isDiscoverable, int memberCount, List<String> members) {
        switch (type) {
            case 0:
                team = new Team(name, place, xp, uid, "Team", memberCount, members, isDiscoverable, role);
                groups.add(team);
                break;
            case 1:
                dogroup = new Dogroup(name, place, xp, uid, "Do-group", memberCount, role);
                groups.add(dogroup);
                break;
            case 2:
                study = new StudyAssociation(name, place, xp, uid, "Study association", memberCount, role);
                groups.add(study);
                break;
            default:
                Log.e(TAG, "Type does not exist.");
        }
        sendGroups();
    }

    /**
     * Checks if the team has enough members to start the quest.
     * @param quest The quest to be played
     */
    public static void checkIfTeamComplete(Quest quest) {
        Puzzle puzzle = quest.getPuzzle(0);
        if (puzzle == null) {
            Log.i(TAG, "Puzzle is null");
        } else {
            // if a singleplayer quest
            if(quest.getMinimumPlayers() == 1) {
                ArrayList<String> teamMembers = new ArrayList<>();
                teamMembers.add(userID);
                checkNearbyQuest(quest, teamMembers, puzzle);
                // if user has team
            } else if (!team.getUid().isEmpty()) {
                teamCol.document(team.getUid()).collection("member").get().addOnCompleteListener(task -> {
                    ArrayList<String> teamMembers = new ArrayList<>();
                    for (DocumentSnapshot docSnap : task.getResult().getDocuments()) {
                        // add team member ids to list
                        teamMembers.add(docSnap.getId());
                    }
                    int memberCount = teamMembers.size();
                    // check if enough team members
                    if (memberCount < quest.getMinimumPlayers()) {
                        mainActivity.toast(quest.getMinimumPlayers() + " team members required!");
                    } else {
                        checkNearbyQuest(quest, teamMembers, puzzle);
                    }
                });
                // no team and not singleplayer quest, team required
            } else {
                // TODO: open profile or settings fragment?
                mainActivity.toast("Go to settings to join a team!");
            }
        }
    }

    /**
     * Checks if the team is nearby enough to play the quest (location-based).
     * In the end, sorts team member list and identifies everyone's role based on the user's
     * index in the ArrayList (so role based on user ID).
     * @param quest Quest to be played
     * @param teamMembers Team members by name in a list
     * @param puzzle The current puzzle
     */
    private static void checkNearbyQuest(Quest quest, ArrayList<String> teamMembers, Puzzle puzzle) {
        // check if user is nearby quest
        DeviceLocation deviceLocation = mainActivity.getPlayFragment().getDeviceLocation();
        if (deviceLocation == null) {
            mainActivity.toast("Still loading");
        } else if (deviceLocation.currentBestLocation == null) {
            mainActivity.toast("GPS location unknown");
        } else {
            LatLng lat = quest.getLocation(); //TODO All this stuff can be in a helper function
            Location newLoc = new Location("");
            newLoc.setLatitude(lat.latitude);
            newLoc.setLongitude(lat.longitude);
            //deviceLocation.startUpdatingLocation();
            float distance = deviceLocation.currentBestLocation.distanceTo(newLoc);
            if (distance > 60) {
                Log.i("JAAP", "distance between quest and me = " + distance);
                mainActivity.toast("Get closer to the quest!");
                return;
            }

            // sort team members list
            Collections.sort(teamMembers);
            // get index of user which is the quest role
            questRole = teamMembers.indexOf(userID);
            mainActivity.setSelectedQuest(quest);
            //init first puzzle
            mainActivity.getPlayFragment().setPuzzle(puzzle);
        }
    }

    /**
     * Sets and stores the current user's uid
     *
     * @param uidUser Unique identifier of user
     */
    public static void setUserID (String uidUser){
        userID = uidUser;
    }

    /**
     * Updates the user's group document reference field. If a user joins a group, this reference will be updated accordingly.
     *
     * @param colRefGroup Collection reference of team, do-group or study association
     * @param uidGroup    Unique identifier of group
     */
    private static void updateGroupReferenceUser (CollectionReference colRefGroup, String uidGroup){
        DocumentReference docRef = colRefGroup.document(uidGroup).collection("member").document(userID);
        userCol.document(userID).update(colRefGroup.getPath() + "_ref", docRef).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                getCollectionReferencesFromUser();
            }
        });
    }

    /**
     * Adds an onSuccessListener to a specific task. The response is success or fail.
     *
     * @param task A Firestore task like set(), get() or delete()
     */
    private static void addSuccessListener (Task task){
        task.addOnSuccessListener(o -> Log.i(TAG, "Task successfully executed!"))
                .addOnFailureListener(e -> Log.e(TAG, "Task failed", e));
    }

    public static int getQuestRole () {
        return questRole;
    }

    /**
     * Returns the team collection reference
     *
     * @return Team collection reference
     */
    public static CollectionReference getTeamCol () {
        return teamCol;
    }

    /**
     * Returns the do-group collection reference
     *
     * @return Do-group collection reference
     */
    public static CollectionReference getDogroupCol () {
        return dogroupCol;
    }

    /**
     * Returns the study association collection reference
     *
     * @return Study association collection reference
     */
    public static CollectionReference getStudyCol () {
        return studyCol;
    }
}
