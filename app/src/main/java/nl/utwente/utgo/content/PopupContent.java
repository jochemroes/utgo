package nl.utwente.utgo.content;

import android.os.Bundle;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.utwente.utgo.Firestore;
import nl.utwente.utgo.MainActivity;
import nl.utwente.utgo.PrettyPrint;
import nl.utwente.utgo.R;
import nl.utwente.utgo.quests.Puzzle;
import nl.utwente.utgo.quests.Quest;
import nl.utwente.utgo.quests.XpQuest;

public class PopupContent extends Content {

    private static final String GPS_TITLE = "GPS";
    private static final String GPS_MESSAGE = "Looking for GPS signal...";
    private static final String GPS_SIGNAL_MESSAGE = "GPS signal found! You can now start playing by selecting a quest.";

    private ViewGroup container;

    private Map<Fragment, Pair<String, List<View>>> viewsMap = new HashMap<>();

    private boolean foundGPS = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.popup_content, container, false);

        this.inflater = inflater;
        contentFiller = view.findViewById(R.id.content_filler);
        scroll = view.findViewById(R.id.scroll);
        this.container = container;
        GPSMessage(((MainActivity) getActivity()).getPlayFragment());
        return view;
    }

    public boolean foundGPS() {
        return foundGPS;
    }

    public void setViews(Fragment frag, String title, List<View> views) {
        Pair<String, List<View>> pair = new Pair<>(title, views);
        viewsMap.put(frag, pair);
    }

    public void setViews(Fragment frag, String title, View view) {
        List<View> views = new ArrayList<>();
        views.add(view);
        Pair<String, List<View>> pair = new Pair<>(title, views);
        viewsMap.put(frag, pair);
    }

    public void show(Fragment frag) {
        Pair<String, List<View>> pair = viewsMap.get(frag);
        contentFiller.removeAllViews();
        for (View v: pair.second) {
            contentFiller.addView(v);
        }
        ((MainActivity) getActivity()).displayPopup(pair.first);
    }

    /**
     * Shows all child views
     * @param view parents view
     */
    public static void showAllViews(LinearLayout view) {
        for (int i = 0; i < view.getChildCount(); i++) {
            view.getChildAt(i).setVisibility(View.VISIBLE);
        }
    }

    /**
     * Hides all child views
     * @param view the parent view
     */
    public static void hideAllViews(LinearLayout view) {
        for (int i = 0; i < view.getChildCount(); i++) {
            view.getChildAt(i).setVisibility(View.GONE);
        }
    }

    public static void hideAllViews(List<View> views) {
        for (View v: views) {
            v.setVisibility(View.GONE);
        }
    }

    /**
     * Creates a text field.
     *
     * @param text value of the text field
     * @return the text field
     */
    public TextView createTextView(String text) {
        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setTextColor(getResources().getColor(R.color.text_color));
        textView.setGravity(Gravity.CENTER);
        return textView;
    }

    /**
     * Creates a button for every hint (2nd hint button only visible when 1st is opened)
     * @param puzzle Puzzle object that contains the hints
     * @param index Used for recursion
     * @return View that contains the buttons and the hints
     */
    public View createHintButton(Puzzle puzzle, int index) {
        List<String> hints = puzzle.getHints();
        if (index < hints.size() && !hints.get(index).equals("")) {
            TextView hintView = createTextView("Hint #" + (index + 1) + ": " + hints.get(index));
            hintView.setVisibility(View.GONE);
            LinearLayout buttonContainer = (LinearLayout) getLayoutInflater().inflate(R.layout.mc_button, container, false);
            buttonContainer.addView(hintView);
            Button button = buttonContainer.findViewById(R.id.mc_button);
            button.setText("Get " + PrettyPrint.numberToOrdinal(index + 1) + " hint");
            button.setOnClickListener(v -> {
                button.setVisibility(View.GONE);
                hintView.setVisibility(View.VISIBLE);
                if (index + 1 < hints.size()) buttonContainer.addView(createHintButton(puzzle, index + 1));
                // TODO possibly decrease score of puzzle
            });
            return buttonContainer;
        }
        return null;
    }

    /**
     * Creates a button that opens the story of a puzzle when the prompt is opened
     * and a button that opens the prompt of a puzzle when the story is opened
     * @param story The story of a puzzle in String form
     * @return the view that contains the buttons and the story text field
     */
    public View createStoryButton(String story, boolean hasPrompt) {
        TextView storyView = createTextView(story);
        LinearLayout storyContainer = (LinearLayout) getLayoutInflater().inflate(R.layout.mc_button, container, false);
        LinearLayout hideButtonContainer = (LinearLayout) getLayoutInflater().inflate(R.layout.mc_button, container, false);
        storyContainer.addView(storyView);

        Button showButton = storyContainer.findViewById(R.id.mc_button);
        Button hideButton = hideButtonContainer.findViewById(R.id.mc_button);

        if (hasPrompt) {
            storyContainer.addView(hideButtonContainer);

            showButton.setText("< story");
            showButton.setOnClickListener(v -> {
                contentFiller.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.exit_lr));
                hideAllViews(contentFiller);
                storyContainer.setVisibility(View.VISIBLE);
                showButton.setVisibility(View.GONE);
                storyView.setVisibility(View.VISIBLE);
                hideButton.setVisibility(View.VISIBLE);
                contentFiller.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.enter_lr));
            });

            hideButton.setText("question >");
            hideButton.setOnClickListener(v -> {
                contentFiller.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.exit_rl));
                showAllViews(contentFiller);
                showButton.setVisibility(View.VISIBLE);
                storyView.setVisibility(View.GONE);
                hideButton.setVisibility(View.GONE);
                contentFiller.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.enter_rl));
            });
        }

        hideAllViews(contentFiller);
        storyContainer.setVisibility(View.VISIBLE);
        storyView.setVisibility(View.VISIBLE);
        showButton.setVisibility(View.GONE);
        if (hasPrompt) {
            hideButton.setVisibility(View.VISIBLE);
        } else {
            hideButton.setVisibility(View.GONE);
        }

        return storyContainer;
    }

    /**
     * Creates an input field
     * @return Input view that calls the submit function of a puzzle when filled in
     */
    public View createInput(Puzzle puzzle, Fragment frag) {
        LinearLayout view = (LinearLayout) getLayoutInflater().inflate(R.layout.text_input, container, false);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
        params.setMargins(0, 0, 0, 10);

        TextView wrongAnswer = view.findViewById(R.id.wrong_answer);
        TextInputLayout inputLayout = view.findViewById(R.id.InputLayout);
        TextInputEditText editText = view.findViewById(R.id.EditText);

        inputLayout.setPlaceholderText(" ");
        inputLayout.setHint("Enter your answer here");
        editText.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                String answer = v.getText().toString();
                handled = puzzle.checkAnswer(answer);
                if (puzzle.getOrder() + 1 == puzzle.getEnclosingQuest().getnPuzzles() && handled) {
                    Quest enclosing = puzzle.getEnclosingQuest();
                    if (enclosing instanceof XpQuest) {
                        XpQuest xpQuest = (XpQuest) enclosing;
                        int xp = xpQuest.getXp();
                        message("Congratulations!", xp + " xp has been added to your account and groups!", frag);
                        show(frag);
                    } else {
                        message("Congratulations!", "You have completed the quest!", frag);
                        show(frag);
                    }
                }
                if (handled) {
                    ((MainActivity) getActivity()).getPlayFragment().removeQuestLocationMarkers();
                    ((MainActivity) getActivity()).getPlayFragment().getAugmentedImage().removeNodesAugmented();
                    ((MainActivity) getActivity()).getPlayFragment().getAugmentedImage().removeAugmentedDataBase();
                } else {
                    wrongAnswer.setVisibility(View.VISIBLE);
                    editText.setText("");
                    inputLayout.setPlaceholderText("something else than \"" + answer + "\"");
                    inputLayout.setSelected(false);
                }
            }
            return handled;
        });

        LinearLayout buttonContainer = (LinearLayout) getLayoutInflater().inflate(R.layout.mc_button, container, false);
        Button button = buttonContainer.findViewById(R.id.mc_button);
        button.setText("Submit answer");
        button.setOnClickListener(v -> editText.onEditorAction(EditorInfo.IME_ACTION_SEND));
        view.addView(buttonContainer);
        return view;
    }

    /**
     * Creates a skip button
     * @return Skip button that goes the next puzzle
     */
    public View createSkipButton(Puzzle puzzle, String text, Fragment frag) {
        LinearLayout buttonContainer = (LinearLayout) getLayoutInflater().inflate(R.layout.mc_button, container, false);
        Button button = buttonContainer.findViewById(R.id.mc_button);
        button.setText(text);
        button.setOnClickListener(v -> {
            puzzle.skip();
            if (puzzle.getOrder() + 1 == puzzle.getEnclosingQuest().getnPuzzles()) {
                Quest enclosing = puzzle.getEnclosingQuest();
                if (enclosing instanceof XpQuest) {
                    XpQuest xpQuest = (XpQuest) enclosing;
                    int xp = xpQuest.getXp();
                    message("Congratulations!", xp + " xp has been added to your account and groups!", frag);
                    show(frag);
                } else {
                    message("Congratulations!", "You have completed the quest!", frag);
                    show(frag);
                }
            }
            ((MainActivity) getActivity()).getPlayFragment().removeQuestLocationMarkers();
            ((MainActivity) getActivity()).getPlayFragment().getAugmentedImage().removeNodesAugmented();
            ((MainActivity) getActivity()).getPlayFragment().getAugmentedImage().removeAugmentedDataBase();
        });
        return buttonContainer;
    }

    public View createStartButton(Quest q) {
        LinearLayout buttonContainer = (LinearLayout) getLayoutInflater().inflate(R.layout.mc_button, container, false);
        Button button = buttonContainer.findViewById(R.id.mc_button);
        button.setText("Start Quest");
        button.setOnClickListener(v -> Firestore.checkIfTeamComplete(q));
        return buttonContainer;
    }

    /**
     * Displays a message in the popup.
     *
     * @param title   Title of the popup
     * @param message Message in the popup
     */
    public void message(String title, String message, Fragment frag) {
        setViews(frag, title, createTextView(message));
    }

    /**
     * Hides the popup if it's the GPS popup.
     */
    public void GPSFound(Fragment frag) {
        if (!foundGPS) {
            message(GPS_TITLE, GPS_SIGNAL_MESSAGE, frag);
            foundGPS = true;
            if (((MainActivity) getActivity()).getCurrentFragment() == frag) {
                ((MainActivity) getActivity()).hidePopup();
            }
        }
    }

    /**
     * Popup that tells the user the application is looking for a GPS signal
     */
    public void GPSMessage(Fragment frag) {
        message(GPS_TITLE, GPS_MESSAGE, frag);
    }

    /**
     * Displays a prompt with input field
     * @param puzzle Puzzle object that has the prompt and verifies the answer
     */
    public void submit(Puzzle puzzle, Fragment frag) {
        Quest quest = puzzle.getEnclosingQuest();
        String title = quest.getTitle()
                + " (" + (puzzle.getOrder() + 1) + "/" + quest.getnPuzzles() + ")";
        int role = Firestore.getQuestRole();
        boolean hasPrompt = puzzle.hasPrompt(role);
        String story = "";
        if (puzzle.getOrder() == 0) story += "Hey, " + Firestore.player.getName() + "!\n\n";
        story += puzzle.getStory();

        List<View> views = new ArrayList<>();
        views.add(createStoryButton(story, hasPrompt));
        if (hasPrompt) {
            String description = puzzle.getPrompt(role);
            views.add(createTextView(description));
            View hintButton = createHintButton(puzzle, 0);
            if (hintButton != null) views.add(hintButton);
            views.add(createInput(puzzle, frag));
            if (puzzle.isSkippable()) views.add(createSkipButton(puzzle, "Skip this question", frag));
        }
        hideAllViews(views);
        views.get(0).setVisibility(View.VISIBLE);
        if (!hasPrompt) {
            views.add(createSkipButton(puzzle, "Next", frag));
        }
        setViews(frag, title, views);
    }

    public void  questInfo(Quest q, Fragment frag) {
        List<View> views = new ArrayList<>();
        views.add(createTextView(q.getDescription()));
        views.add(createStartButton(q));
        setViews(frag, q.getTitle(), views);
    }

}
