package nl.utwente.utgo.content;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.utwente.utgo.Firestore;
import nl.utwente.utgo.GoogleLogin;
import nl.utwente.utgo.MainActivity;
import nl.utwente.utgo.PrettyPrint;
import nl.utwente.utgo.R;
import nl.utwente.utgo.profile.Dogroup;
import nl.utwente.utgo.profile.Group;
import nl.utwente.utgo.profile.Player;
import nl.utwente.utgo.profile.StudyAssociation;
import nl.utwente.utgo.profile.Team;

public class SettingsContent extends Content {

    private String[] studyAssociations;
    private String[] doGroups;
    private String[] teams;

    private String[] studyAssociationIDs = new String[0];
    private String[] doGroupIDs = new String[0];
    private String[] teamIDs = new String[0];

    public enum Query {
        TEAM, DOGROUP, STUDY
    }

    private enum Method {
        CHANGEUSERNAME,
        CHANGETEAMNAME, SETTEAMDISCOVERABLE,
        PICKTEAM, MAKENEWTEAM,
        PICKSTUDYASS, PICKDOGROUP
    }

    private Map<Method, String[]> methodMap = new HashMap<>();

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        methodMap.put(Method.CHANGEUSERNAME, new String[] {"Change username", "Change your username to \"", "\"?"});
        methodMap.put(Method.CHANGETEAMNAME, new String[] {"Change team name", "Change your team name to \"", "\"?"});
        methodMap.put(Method.SETTEAMDISCOVERABLE, new String[] {"Change team privacy", "Make your team ", "?"});
        methodMap.put(Method.PICKTEAM, new String[] {"Switch team", "Join \"", "\" and leave your current team?"});
        methodMap.put(Method.MAKENEWTEAM, new String[] {"Create new team", "Create the new team \"", "\" and leave your current team?"});
        methodMap.put(Method.PICKSTUDYASS, new String[] {"Switch study association", "Join \"", "\" and leave your current study association?"});
        methodMap.put(Method.PICKDOGROUP, new String[] {"Switch do-group", "Join \"", "\" and leave your currrent do-group?"});
        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * Loads in all the settings
     */
    @Override
    public void loadContent() {
        if (contentFiller == null) return;
        setListTeam(Firestore.getSettingsList(Query.TEAM));
        setListDoGroup(Firestore.getSettingsList(Query.DOGROUP));
        setListStudy(Firestore.getSettingsList(Query.STUDY));
        contentFiller.removeAllViews();
        addInput("Change username", getPlayer().getName(), Method.CHANGEUSERNAME);
        addEmptySpace();
        if (getTeam().getRole() >= 1) {
            addInput("Change team name", getTeam().getName(), Method.CHANGETEAMNAME);
            addSwitch("Public team (anyone can join, max. " + Firestore.getMaxTeamSize() + " players)", getTeam().isDiscoverable(), Method.SETTEAMDISCOVERABLE);
            addEmptySpace();
        }
        addSpinner(teams, Method.PICKTEAM, teamIDs);
        addInput("Create new team", "New team name", Method.MAKENEWTEAM);
        addEmptySpace();
        addSpinner(studyAssociations, Method.PICKSTUDYASS, studyAssociationIDs);
        addEmptySpace();
        addSpinner(doGroups, Method.PICKDOGROUP, doGroupIDs);
        addEmptySpace();
        addReviewButton();
        addEmptySpace();
        addSignOutButton();
        addDeleteAccountButton();
    }

    /**
     * Adds an input field to the settings page
     * @param hint hint for the input field
     * @param placeholder placeholder for the input field
     * @param method enum that defines what method to call with the answer
     */
    public void addInput(String hint, String placeholder, Method method) {
        View view = inflater.inflate(R.layout.text_input, null);
        //FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
        //params.setMargins(0, 0, 0, 10);

        TextInputLayout inputLayout = view.findViewById(R.id.InputLayout);
        TextInputEditText editText = view.findViewById(R.id.EditText);

        inputLayout.setPlaceholderText(placeholder);
        inputLayout.setHint(hint);
        editText.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                confirmWindow(method, v.getText().toString(), null);
                handled = true;
            }
            return handled;
        });
        contentFiller.addView(view);
    }

    /**
     * Adds a switch to the settings page
     * @param text description what the switch is for
     * @param checked if the switch is checked or not
     * @param method enum that defines what method to call with the answer
     */
    public void addSwitch(String text, boolean checked, Method method) {
        View view = inflater.inflate(R.layout.switch_input, null);
        Switch switchInput = view.findViewById(R.id.SwitchInput);

        switchInput.setText(text);
        switchInput.setChecked(checked);
        switchInput.setOnCheckedChangeListener((buttonView, isChecked) -> confirmWindow(method, isChecked, null));

        contentFiller.addView(view);
    }

    /**
     * Adds a selection menu to the settings page
     * @param items list of options in the menu
     * @param method enum that defines what method to call with the answer
     * @param IDs list of IDs corresponding to each option in the menu
     */
    public void addSpinner(String[] items, Method method, String[] IDs) {
        View view = inflater.inflate(R.layout.spinner, null);
        Spinner spinner = view.findViewById(R.id.spinner);
        ArrayAdapter adapter = new ArrayAdapter(getContext(), R.layout.spinner_item, items);

        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position != 0) {
                    // Calling replace here is an ugly and hardcoded solution.
                    // But shouldn't cause that many problems, unless people start calling
                    // their team " (current)". And even then won't destroy anything just
                    // gives a confusing message in the confirm popup.
                    confirmWindow(method, items[position].replace(" (current)", ""), IDs[position]);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // No setting needs to be changed
            }
        });

        contentFiller.addView(view);
    }

    /**
     * Adds an empty space to the settings page
     */
    public void addEmptySpace() {
        View view = inflater.inflate(R.layout.empty_space, null);
        contentFiller.addView(view);
    }

    /**
     * Adds a signout button
     */
    private void addSignOutButton() {
        View view = inflater.inflate(R.layout.sign_out_button, null);
        Button button = view.findViewById(R.id.sign_out);
        button.setOnClickListener(view1 -> {
            GoogleLogin.signOut();
        });
        contentFiller.addView(view);
    }

    /**
     * Adds a delete account button
     */
    private void addDeleteAccountButton() {
        View view = inflater.inflate(R.layout.delete_account_button, null);
        Button button = view.findViewById(R.id.delete_account);
        button.setOnClickListener(view1 -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.DialogTheme);
            builder.setCancelable(true);
            builder.setTitle("Delete account");
            builder.setMessage("Are you sure? This will permanently delete your account.");
            builder.setPositiveButton("Confirm", (dialog, which) -> {
                Firestore.deleteUser(); // this also deletes Google account from Firebase
            });
            builder.setNegativeButton("Cancel", null);
            AlertDialog dialog = builder.create();
            dialog.setOnShowListener(arg0 -> {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getContext().getColor(R.color.text_color));
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getContext().getColor(R.color.text_color));
            });
            dialog.show();
        });
        contentFiller.addView(view);
    }

    /**
     * Adds a review button (opens a link)
     */
    private void addReviewButton() {
        View view = inflater.inflate(R.layout.sign_out_button, null);
        Button button = view.findViewById(R.id.sign_out);
        button.setText("Give us feedback!");
        button.setOnClickListener(view1 -> {
            Uri uri = Uri.parse("https://forms.gle/CdHAaVmMyguszk1B9"); // missing 'http://' will cause crash
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });
        contentFiller.addView(view);
    }

    /**
     * Displays confirmation window before submitting change
     * @param onConfirm Enum used to call a function once on confirm
     * @param arg Argument used in that function
     * @param id Argument used in select
     */
    public void confirmWindow(Method onConfirm, Object arg, String id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.DialogTheme);
        builder.setCancelable(true);
        String[] strings = methodMap.get(onConfirm);
        builder.setTitle(strings[0]);
        builder.setMessage(strings[1] + PrettyPrint.settingsValuesToString(arg) + strings[2]);
        if (id != null) {
            builder.setPositiveButton("Confirm", (dialog, which) -> callMethod(onConfirm, id));
        } else {
            builder.setPositiveButton("Confirm", (dialog, which) -> callMethod(onConfirm, arg));
        }
        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(arg0 -> {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getContext().getColor(R.color.text_color));
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getContext().getColor(R.color.text_color));
        });
        dialog.show();
    }

    /**
     * Gets the Player class belonging to the user
     * @return the Player class
     */
    public Player getPlayer() {
        for (Group group : Firestore.getGroups()) {
            if (group instanceof Player) {
                return (Player) group;
            }
        }
        return null;
    }

    /**
     * Gets the Team class belonging to the user
     * @return the Team class
     */
    public Team getTeam() {
        for (Group group : Firestore.getGroups()) {
            if (group instanceof Team) {
                return (Team) group;
            }
        }
        return null;
    }

    /**
     * Gets the Dogroup class belonging to the user
     * @return the Dogroup class
     */
    public Dogroup getDogroup() {
        for (Group group : Firestore.getGroups()) {
            if (group instanceof Dogroup) {
                return (Dogroup) group;
            }
        }
        return null;
    }

    /**
     * Gets the Study (association) class belonging to the user
     * @return the Study class
     */
    public StudyAssociation getStudy() {
        for (Group group : Firestore.getGroups()) {
            if (group instanceof StudyAssociation) {
                return (StudyAssociation) group;
            }
        }
        return null;
    }

    /**
     * Sets the options for study associations
     * @param list list of study associations gotten from server
     */
    public void setListStudy(List<Object[]> list) {
        studyAssociations = new String[list.size() + 1];
        studyAssociationIDs = new String[list.size() + 1];
        studyAssociations[0] = "Select study association...";
        for (int i = 0; i < list.size(); i++) {
            studyAssociationIDs[i+1] = (String) list.get(i)[0];
            studyAssociations[i+1] = (String) list.get(i)[1];
        }
    }

    /**
     * Sets the options for dogroups
     * @param list list of dogroups gotten from server
     */
    public void setListDoGroup(List<Object[]> list) {
        doGroups = new String[list.size() + 1];
        doGroupIDs = new String[list.size() + 1];
        doGroups[0] = "Select do-group...";
        for (int i = 0; i < list.size(); i++) {
            doGroupIDs[i+1] = (String) list.get(i)[0];
            doGroups[i+1] = (String) list.get(i)[1];
        }
    }

    /**
     * Sets the options for teams
     * @param list list of teams gotten from server
     */
    public void setListTeam(List<Object[]> list) {
        teams = new String[list.size() + 1];
        teamIDs = new String[list.size() + 1];
        teams[0] = "Join (other) existing team...";
        for (int i = 0; i < list.size(); i++) {
            teamIDs[i+1] = (String) list.get(i)[0];
            teams[i+1] = (String) list.get(i)[1];
        }
    }

    /**
     * Calls a method
     * @param method enum that defines what method to call
     * @param arg argument that is casted to the input for the chosen method
     */
    public void callMethod(Method method, Object arg) {
        switch(method) {
            case CHANGEUSERNAME:
                if (!Firestore.changeUsername((String) arg))
                    ((MainActivity) getActivity()).toast("Name not valid!");
                break;
            case CHANGETEAMNAME:
                if (groupExists(getTeam()) && !Firestore.changeGroupName(Firestore.getTeamCol(), getTeam().getUid(), (String) arg))
                    ((MainActivity) getActivity()).toast("Name not valid!");
                break;
            case SETTEAMDISCOVERABLE:
                if(groupExists(getTeam())) {
                    Firestore.changeDiscoverabilityTeam(getTeam().getUid(), (Boolean) arg);
                }
                break;
            case PICKTEAM:
                if(groupExists(getTeam())) {
                    Firestore.joinGroup(Firestore.getTeamCol(), (String) arg, getTeam().getUid());
                }
                break;
            case MAKENEWTEAM:
                if(groupExists(getTeam())) {
                    Firestore.createGroup((String) arg, Firestore.getTeamCol(), getTeam().getUid());
                }
                break;
            case PICKSTUDYASS:
                if(groupExists(getStudy())) {
                    Firestore.joinGroup(Firestore.getStudyCol(), (String) arg, getStudy().getUid());
                }
                break;
            case PICKDOGROUP:
                if(groupExists(getDogroup())) {
                    Firestore.joinGroup(Firestore.getDogroupCol(), (String) arg, getDogroup().getUid());
                }
                break;
        }
    }

    /**
     * Check needed if user joins group and immediately after that tries to join other group.
     * Otherwise local groups stored will temporarily be null and cause null pointer exception on group.getUid().
     * @param group Group
     * @return If the group is not null
     */
    private boolean groupExists(Group group) {
        return group != null;
    }
}
