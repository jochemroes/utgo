package nl.utwente.utgo.content;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import nl.utwente.utgo.Firestore;
import nl.utwente.utgo.MainActivity;
import nl.utwente.utgo.PrettyPrint;
import nl.utwente.utgo.R;
import nl.utwente.utgo.profile.Group;
import nl.utwente.utgo.profile.Player;
import nl.utwente.utgo.profile.Team;

import static nl.utwente.utgo.PrettyPrint.integerPrettyPrint;

public class ProfileContent extends Content {

    private enum ONCLICK {
        openSettings, toggleSize
    }

    @Override
    public void loadContent() {
        if (contentFiller == null) return;
        contentFiller.removeAllViews();
        addGroupByTitle("Player");
        addGroupByTitle("Team");
        addGroupByTitle("Do-group");
        addGroupByTitle("Study association");
    }

    /**
     * Adds a card based on the title of the group it displays ("Player", "Team", etc.)
     * @param type Group title
     */
    public void addGroupByTitle(String type) {
        LayoutInflater inflater = getLayoutInflater();
        for (Group group : Firestore.getGroups()) {
            if (type.equals(group.getTitle())) {
                if (group instanceof Player) {
                    addCard(inflater.inflate(R.layout.card_big, null), group.getTitle(), group.getName(), group.getPlace(), group.getScore(), null, ONCLICK.openSettings);
                } else if (group.getPlace() == -1) {
                    addCard(inflater.inflate(R.layout.card_empty, null), null, "Join a " + group.getTitle().toLowerCase(), -1, -1, null, ONCLICK.openSettings);
                } else if (group instanceof Team) {
                    addCard(inflater.inflate(R.layout.card_group, null)
                            , group.getTitle()
                            , group.getName()
                            , group.getPlace()
                            , group.getScore()
                            , PrettyPrint.membersPrettyPrint(((Team) group).getMembers())
                            , ONCLICK.toggleSize);
                } else {
                    addCard(inflater.inflate(R.layout.card_group, null)
                            , group.getTitle()
                            , group.getName()
                            , group.getPlace()
                            , group.getScore()
                            , PrettyPrint.memberCountPrettyPrint(group.getMemberCount(), group.getTitle())
                            , ONCLICK.toggleSize);
                }
                break;
            }
        }
    }

    /**
     * Adds a card to profile content
     * @param card Card view
     * @param title Title of the card
     * @param name Name of the player or group
     * @param place Place of the player or group
     * @param score XP Score of the player or group
     * @param desc Description of the group
     * @param onClick Enum that decides what function to call once clicked
     */
    public void addCard(View card, String title, String name, int place, int score, String desc, ONCLICK onClick) {
        card.setBackground(getCardBackground());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.card_margin_b));
        card.setLayoutParams(params);

        TextView titleField = card.findViewById(R.id.Title);
        TextView nameField = card.findViewById(R.id.Name);
        TextView placeField = card.findViewById(R.id.Place);
        TextView scoreField = card.findViewById(R.id.Score);

        if (titleField != null) titleField.setText(title);
        if (nameField != null) nameField.setText(name);
        if (place != -1) placeField.setText("#" + place);
        if (score != -1) scoreField.setText(integerPrettyPrint(score) + "\nXP");

        if (onClick == ONCLICK.openSettings) {
            card.setOnClickListener(((MainActivity) getActivity()).settingsClickedListener);
        }
        TextView description = card.findViewById(R.id.description);
        if (onClick == ONCLICK.toggleSize && description != null) {
            description.setText(desc);
            card.setOnClickListener(getGroupCardListener(card));
        }

        contentFiller.addView(card);
    }

    /**
     * Gets the background for a card
     * @return Background drawable
     */
    public Drawable getCardBackground() {
        LayerDrawable bg = (LayerDrawable) getContext().getDrawable(R.drawable.card_background);
        bg.setDrawableByLayerId(R.id.accentColor, null);
        return bg;
    }

    /**
     * Toggles height for group card
     * @param card Card view which height will change
     * @return Listener that changes the height of a group card
     */
    public View.OnClickListener getGroupCardListener(View card) {
        return v -> {
            int descriptionHeightCon = getResources().getDimensionPixelSize(R.dimen.mid_description_height_con);
            int descriptionHeightExp = getResources().getDimensionPixelSize(R.dimen.mid_description_height_exp);

            for (int i = 0; i < contentFiller.getChildCount(); i++) {
                View tmpCard = ((View) contentFiller.getChildAt(i));
                TextView description = tmpCard.findViewById(R.id.description);
                if (description != null) {
                    if (card.equals(tmpCard) && description.getLayoutParams().height == descriptionHeightCon) {
                        animHeight(description, descriptionHeightCon, descriptionHeightExp, card);
                    } else {
                        animHeight(description, descriptionHeightExp, descriptionHeightCon, card);
                    }
                }

            }
        };
    }

}
