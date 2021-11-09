package nl.utwente.utgo.content;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import nl.utwente.utgo.Firestore;
import nl.utwente.utgo.R;

import static nl.utwente.utgo.PrettyPrint.integerPrettyPrint;

public class LeaderboardsContent extends Content {

    public enum Query {PLAYERS, TEAMS, DOGROUPS, ASSOCIATIONS}
    private Query[] queries = {Query.PLAYERS, Query.TEAMS, Query.DOGROUPS, Query.ASSOCIATIONS};
    private Query query;

    public LeaderboardsContent(int position) {
        query = queries[position];
    }

    @Override
    public void loadContent() {
        if (contentFiller == null) return;
        List<Object[]> leaderboard = Firestore.getLeaderboard(query);
        contentFiller.removeAllViews();
        Object[] values;
        if (leaderboard.size() > 0) {
            values = leaderboard.get(0);
            View firstPlaceCard = inflater.inflate(R.layout.card_big, null);
            firstPlaceCard.setBackground(getCardBackground("#D4AF37", (Boolean) values[2]));
            addCard(firstPlaceCard, null, (String) values[0], 1, (Integer) values[1]);
        }
        if (leaderboard.size() > 1) {
            values = leaderboard.get(1);
            View secondPlaceCard = inflater.inflate(R.layout.card_small, null);
            secondPlaceCard.setBackground(getCardBackground("#C0C0C0", (Boolean) values[2]));
            addCard(secondPlaceCard, null, (String) values[0], 2, (Integer) values[1]);
        }
        if (leaderboard.size() > 2) {
            values = leaderboard.get(2);
            View thirdPlaceCard = inflater.inflate(R.layout.card_small, null);
            thirdPlaceCard.setBackground(getCardBackground("#CD7F32", (Boolean) values[2]));
            addCard(thirdPlaceCard, null, (String) values[0], 3, (Integer) values[1]);
        }
        for (int i = 3; i < leaderboard.size(); i++) {
            values = leaderboard.get(i);
            View placeCard = inflater.inflate(R.layout.card_small, null);
            boolean highlighted = ((Boolean) values[2]);
            placeCard.setBackground(getCardBackground(null, highlighted));
            addCard(placeCard, null, (String) values[0], i + 1, (Integer) values[1]);
            if (highlighted) {
                focusOnView(placeCard);
            }
        }
    }

    /**
     * Adds a leaderboard card
     * @param card Card view
     * @param title Title of the card (not used here, keep null)
     * @param name Name of the player/group
     * @param place Place (#1, #2, #3, etc.)
     * @param score XP score
     */
    public void addCard(View card, String title, String name, int place, int score) {
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

        if (titleField != null) {
            titleField.setText(title);
        }
        nameField.setText(name);
        placeField.setText("#" + place);
        scoreField.setText(integerPrettyPrint(score) + "\nXP");

        contentFiller.addView(card);
    }

    /**
     * Gets the background for a card
     * @param accentColor Color on the bottom of a card (gold, silver, bronze or none)
     * @param highlighted If the card is highlighted or not
     * @return Background drawable
     */
    public Drawable getCardBackground(String accentColor, boolean highlighted) {
        LayerDrawable bg;
        if (highlighted) {
            bg = (LayerDrawable) getContext().getDrawable(R.drawable.card_highlighted_background);
        } else {
            bg = (LayerDrawable) getContext().getDrawable(R.drawable.card_background);
        }
        if (accentColor != null) {
            float r = getResources().getDimensionPixelSize(R.dimen.card_border_radius);
            ShapeDrawable shape = new ShapeDrawable(new RoundRectShape(new float[]{r, r, r, r, r, r, r, r}, null, null));
            shape.getPaint().setColor(Color.parseColor(accentColor));
            bg.setDrawableByLayerId(R.id.accentColor, shape);
        } else {
            bg.setDrawableByLayerId(R.id.accentColor, null);
        }
        return bg;
    }

    @Override
    public void focusOnView(View view) {
        scroll.post(() -> scroll.scrollTo(0, view.getTop() - scroll.getHeight() / 2));
    }

}
