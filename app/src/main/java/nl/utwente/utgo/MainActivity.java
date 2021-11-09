package nl.utwente.utgo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nl.utwente.utgo.arcorelocation.utils.ARLocationPermissionHelper;
import nl.utwente.utgo.quests.Quest;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main Activity";
    private static final int RC_LOGIN_ACT = 1;
    private QuestsFragment quests = QuestsFragment.newInstance("", "");
    private MapFragment map = MapFragment.newInstance("", "");
    private PlayFragment play = PlayFragment.newInstance("", "");
    private LeaderboardsFragment leaderboards = LeaderboardsFragment.newInstance("", "");
    private ProfileFragment profile = ProfileFragment.newInstance("", "");
    private SettingsFragment settings = SettingsFragment.newInstance("", "");
    private List<Integer> pageStack = new ArrayList<>();
    private BottomNavigationView navigation;
    private ImageView leftIcon;
    private ImageView rightIcon;
    private Quest selectedQuest;

    View titleCard;
    View titleCardInner;
    TextView titleContent;
    View bottomMenu;

    Animation fadeIn;
    Animation fadeOut;

    /**
     * Listener for the bottom navigation that opens a page when it is selected
     */
    private BottomNavigationView.OnNavigationItemSelectedListener itemSelectedListener = item -> {
        Fragment current = getCurrentFragment();

        java.util.Map<Integer, Object[]> valueMap = new HashMap<>();
        valueMap.put(R.id.quests, new Object[] {quests, false, false});
        valueMap.put(R.id.map, new Object[] {map, true, current instanceof QuestsFragment});
        valueMap.put(R.id.play, new Object[] {play, true, current instanceof QuestsFragment || current instanceof MapFragment});
        valueMap.put(R.id.leaderboards, new Object[] {leaderboards, false, !(current instanceof ProfileFragment || current instanceof SettingsFragment)});
        valueMap.put(R.id.profile, new Object[] {profile, false, !(current instanceof SettingsFragment)});

        Object[] values = valueMap.get(item.getItemId());
        Fragment selected = (Fragment) values[0];
        Boolean useGradient = (Boolean) values[1];
        Boolean slideRight = (Boolean) values[2];

        if (current.equals(selected)) {
            return true;
        }
        if (selected instanceof  FullScreenFragment) {
            if (current instanceof FullScreenFragment) {
                ((FullScreenFragment) selected).setCoverAnimation(false);
            } else {
                ((FullScreenFragment) selected).setCoverAnimation(true);
            }
        }

        pageStack.remove(Integer.valueOf(item.getItemId()));
        pageStack.add(0, item.getItemId());

        fragmentTransaction(selected, useGradient, slideRight);

        return true;
    };

    public View.OnClickListener settingsClickedListener = v -> toggleSettings();

    /**
     * @return The Quests Fragment
     */
    public QuestsFragment getQuestsFragment() {
        return quests;
    }

    /**
     * @return The Play Fragment
     */
    public PlayFragment getPlayFragment() { return play; }

    /**
     * @return The Profile Fragment
     */
    public ProfileFragment getProfileFragment() {
        return profile;
    }

    /**
     * @return The Map Fragment
     */
    public MapFragment getMapFragment() { return map; }

    /**
     * @return The Leaderboards Fragment
     */
    public LeaderboardsFragment getLeaderboardsFragment() { return leaderboards; }

    /**
     * @return The Settings Fragment
     */
    public SettingsFragment getSettingsFragment() { return settings; }

    /**
     * Sets a quest as selected and opens the Play fragment
     * @param quest The quest
     */
    public void setSelectedQuest(Quest quest) {
        if (quest != null) {
            selectedQuest = quest;
            //toast("Current quest: " + selectedQuest.getTitle());
            navigation.setSelectedItemId(R.id.play);
        }
    }

    /**
     * Switches between the settings and profile fragments
     */
    private void toggleSettings() {
        Fragment current = getCurrentFragment();

        if (current.equals(settings)) {
            pageStack.remove(Integer.valueOf(R.id.settings));
            pageStack.remove(Integer.valueOf(R.id.profile));
            pageStack.add(0, R.id.profile);
            fragmentTransaction(profile, false, false);
        } else {
            pageStack.remove(Integer.valueOf(R.id.settings));
            pageStack.add(0, R.id.settings);
            fragmentTransaction(settings, false, true);
        }
    }

    Toast toast;

    /**
     * Displays a message
     * @param msg The message
     */
    public void toast(String msg) {
        if (toast != null) toast.cancel();
        toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    /**
     * Returns the currently selected and displayed fragment
     * @return
     */
    public Fragment getCurrentFragment() {
        for (String tag : new String[] {"Quests", "Map", "Play", "Leaderboards", "Profile", "Settings"}) {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
            if (fragment.isVisible()) {
                System.err.println("Current is: " + fragment.getTag());
                return fragment;
            }
        }
        return null;
    }

    int prevLeftIconSrc;
    String prevTitle;
    int prevRightIconSrc;

    /**
     * Changes the icons and title of the title bar
     * @param leftIconSrc Image src for the left icon
     * @param title Title
     * @param rightIconSrc Image src for the right icon
     * @param rightListener Listener that performs an action when the right icon is pressed
     */
    public void changeTitleBar(int leftIconSrc, String title, int rightIconSrc, View.OnClickListener rightListener) {
        if (leftIconSrc != prevLeftIconSrc) changeLeftIcon(leftIconSrc);
        if (title != null && !title.equals(prevTitle)) changeTitle(title);
        if (rightIconSrc != prevRightIconSrc) changeRightIcon(rightIconSrc);
        if (rightListener != null) rightIcon.setOnClickListener(rightListener);
    }

    /**
     * Changes the left icon of the title bar
     * @param leftIconSrc Image src for the icon
     */
    public void changeLeftIcon(int leftIconSrc) {
        leftIcon.startAnimation(fadeOut);
        if (leftIconSrc != -1) {
            leftIcon.setImageDrawable(getApplicationContext().getDrawable(leftIconSrc));
            leftIcon.setVisibility(View.VISIBLE);
            leftIcon.startAnimation(fadeIn);
        } else {
            leftIcon.setVisibility(View.INVISIBLE);
        }
        prevLeftIconSrc = leftIconSrc;
    }

    /**
     * Changes the title of the title bar
     * @param title The title
     */
    public void changeTitle(String title) {
        titleContent.startAnimation(fadeOut);
        titleContent.setText(title);
        titleContent.startAnimation(fadeIn);
        prevTitle = title;
    }

    /**
     * Changes the right icon of the title bar
     * @param rightIconSrc Image src for the icon
     */
    public void changeRightIcon(int rightIconSrc) {
        rightIcon.startAnimation(fadeOut);
        if (rightIconSrc != -1) {
            rightIcon.setImageDrawable(getApplicationContext().getDrawable(rightIconSrc));
            rightIcon.setVisibility(View.VISIBLE);
            rightIcon.startAnimation(fadeIn);
        } else {
            rightIcon.setVisibility(View.INVISIBLE);
        }
        prevRightIconSrc = rightIconSrc;
    }

    /**
     * Selects and displays a fragment
     * @param selected Fragment to be selected
     * @param useGradient If the title bar uses a gradient (for Map and Play) or is filled
     * @param slideRight If the animation for the transaction slides right (if not it slides left)
     */
    public void fragmentTransaction(Fragment selected, boolean useGradient, boolean slideRight) {
        View.OnClickListener listener = null;
        int rightIconSrc = -1;
        if (selected.equals(profile)) {
            rightIconSrc = R.drawable.ic_baseline_settings_24;
            listener = settingsClickedListener;
        }
        if (selected.equals(settings)) {
            rightIconSrc = R.drawable.ic_baseline_close_24;
            listener = settingsClickedListener;
        }
        changeTitleBar(-1, selected.getTag(), rightIconSrc, listener);

        if (useGradient) {
            titleCard.setBackground(getResources().getDrawable(R.drawable.gradient_from_top));
            bottomMenu.setBackground(getResources().getDrawable(R.drawable.gradient_from_bottom));
        } else {
            titleCard.setBackgroundColor(Color.parseColor("#1C1C1C"));
            bottomMenu.setBackgroundColor(Color.parseColor("#1C1C1C"));
        }
        int[] anim;
        if (slideRight) {
            anim = new int[] {R.anim.enter_rl, R.anim.exit_lr, R.anim.enter_lr, R.anim.exit_rl};
        } else {
            anim = new int[] {R.anim.enter_lr, R.anim.exit_rl, R.anim.enter_rl, R.anim.exit_lr};
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .setCustomAnimations(anim[0], anim[1], anim[2], anim[3])
                .hide(quests)
                .hide(map)
                .hide(play)
                .hide(leaderboards)
                .hide(profile)
                .hide(settings)
                .show(selected)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (pageStack.size() < 2) {
            super.onBackPressed();
            return;
        }

        pageStack.remove(0);
        
        if (pageStack.get(0).equals(R.id.settings)) {
            toggleSettings();
        } else {
            navigation.setSelectedItemId(pageStack.get(0));
        }

    }

    /**
     * Opens the map and select the marker related to a quest
     * @param quest The quest of which the marker is to be selected
     */
    public void openQuestInMap(Quest quest) {
        map.selectMarker(quest);
        navigation.setSelectedItemId(R.id.map);
    }

    /**
     * Class that can hide the splash screen logo in parallel
     */
    public class LogoHider implements Runnable {
        @Override
        public void run() {
            hideLogoAnimation();
        }
    }

    /**
     * Hide the splash screen logo
     */
    public void hideLogoAnimation() {
        int dimen = getResources().getDimensionPixelSize(R.dimen.splash_screen_logo_dimen);
        View view = findViewById(R.id.CoverLogo);
        ViewGroup.LayoutParams params = view.getLayoutParams();
        ValueAnimator anim = ValueAnimator.ofInt(dimen, 0);
        anim.addUpdateListener(valueAnimator -> {
            int val = (Integer) valueAnimator.getAnimatedValue();
            params.width = val;
            params.height = val;
            view.setLayoutParams(params);
        });
        anim.addListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd(Animator animation)
            {
                view.setVisibility(View.INVISIBLE);
            }
        });
        anim.setStartDelay(200);
        anim.setDuration(1000);
        anim.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GoogleLogin.initGoogleLogin(this);
        // first check if user was already signed in before starting the rest of Main Activity
        if (!GoogleLogin.isSignedIn()) {
            Log.i(TAG, "User is not signed in. Going to LoginActivity.");
            startActivityForResult(new Intent(this, LoginActivity.class), RC_LOGIN_ACT);
        } else {
            Log.i(TAG, "User already signed in. Staying in MainActivity");
            initActivity();
        }
    }

    /**
     * Steps to take if the user is logged in
     */
    private void initActivity() {
        setTheme(R.style.Theme_LocationbasedARGame);
        setContentView(R.layout.activity_main);

        new Handler().postDelayed(new LogoHider(), 100);

        View cover = findViewById(R.id.CoverMain);
        Animation coverFadeOut = AnimationUtils.loadAnimation(this, R.anim.cover_fade_out);
        cover.startAnimation(coverFadeOut);
        cover.setVisibility(View.INVISIBLE);
        ARLocationPermissionHelper.requestPermission(this);

        titleCard = findViewById(R.id.TitleCard);
        titleCardInner = findViewById(R.id.TitleCardInner);
        titleContent = findViewById(R.id.TitleContent);
        bottomMenu = findViewById(R.id.bottomNavigationView);
        fadeIn = AnimationUtils.loadAnimation(getBaseContext(), R.anim.title_bar_fade_in);
        fadeOut = AnimationUtils.loadAnimation(getBaseContext(), R.anim.title_bar_fade_out);

        pageStack.add(R.id.quests);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, quests, "Quests")
                .add(R.id.fragment_container, map, "Map")
                .add(R.id.fragment_container, play, "Play")
                .add(R.id.fragment_container, leaderboards, "Leaderboards")
                .add(R.id.fragment_container, profile, "Profile")
                .add(R.id.fragment_container, settings, "Settings")
                .hide(map)
                .hide(play)
                .hide(leaderboards)
                .hide(profile)
                .hide(settings)
                .commit();

        // Hide the android studio title bar since we made our own
        getSupportActionBar().hide();

        // Activate listener for button presses on navigation bar
        navigation = findViewById(R.id.bottomNavigationView);
        navigation.setOnNavigationItemSelectedListener(itemSelectedListener);

        leftIcon = findViewById(R.id.left_icon);
        leftIcon.setVisibility(View.INVISIBLE);

        // Hide and activate listener for settings button
        rightIcon = findViewById(R.id.right_icon);
        rightIcon.setVisibility(View.INVISIBLE);

        // load data
        Firestore.setFragments(this);
        Firestore.getAllData();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RC_LOGIN_ACT) {
            initActivity();
            String name = data.getStringExtra("name");
            String email = data.getStringExtra("email");
            String uid = data.getStringExtra("uid");
            String photo = data.getStringExtra("photo");

            Log.d(TAG, "Hello " + name + " with email " + email + ". Unique identifier is " + uid + " and photo URL " + photo);

            // TODO: Do something with Google account data
        }
    }


    /*private void addStudyAssociations() {
        String[] list = new String[] {
                "Abacus", "Communique", "Paradoks", "Scintilla", "Dimensie", "Alembic", "Atlantis", "Arago",
                "Astatine", "Proto", "Daedalus", "Ideefiks", "Isaac Newton", "Komma", "H.V. Ockham", "SAB",
                "Sirius", "Stress", "ConcepT", "Inter-Actief"};
        for(String groupName : list) {
            Map<String, Object> groupMap = new HashMap<>();
            groupMap.put("name", groupName);
            groupMap.put("xp", 0);
            groupMap.put("member_count", 0);
            Firestore.getStudyCol().add(groupMap);
        }
    }*/
}