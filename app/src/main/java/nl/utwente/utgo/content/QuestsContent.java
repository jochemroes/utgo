package nl.utwente.utgo.content;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import nl.utwente.utgo.Firestore;
import nl.utwente.utgo.MainActivity;
import nl.utwente.utgo.R;
import nl.utwente.utgo.quests.Quest;
import nl.utwente.utgo.quests.RewardQuest;
import nl.utwente.utgo.quests.XpQuest;

import static nl.utwente.utgo.PrettyPrint.integerPrettyPrint;
import static nl.utwente.utgo.PrettyPrint.timePrettyPrint;

public class QuestsContent extends Content {

    public enum Query {ALL, XP, REWARD}
    public enum Sort {TIME, XP}
    private Query[] queries = {Query.ALL, Query.XP, Query.REWARD};
    private Query query;
    private Sort sort;

    public QuestsContent(int position) {
        query = queries[position];
        if (query == Query.XP) {
            sort = Sort.XP;
        } else {
            sort = Sort.TIME;
        }
    }

    @Override
    public void loadContent() {
        if (contentFiller == null) return;
        List<Quest> quests = Firestore.getQuestsList();
        List<Quest> toDisplay = new ArrayList<>();

        if (query == Query.XP) {
            for(Quest q : quests) {
                if (q instanceof XpQuest) { toDisplay.add(q); }
            }
            toDisplay.sort((o1, o2) -> {
                XpQuest o3 = (XpQuest) o1;
                XpQuest o4 = (XpQuest) o2;
                if (o3.getXp() > o4.getXp()) {
                    return -1;
                } else if (o3.getXp() == o4.getXp()) {
                    return 0;
                } else {
                    return 1;
                }
            });
        } else {
            if (query == Query.ALL) {
                toDisplay.addAll(quests);
            } else {
                for(Quest q : quests) {
                    if (q instanceof RewardQuest) { toDisplay.add(q); }
                }
            }
            toDisplay.sort((o1, o2) -> {
                if (o1.getUntil() > o2.getUntil()) {
                    return 1;
                } else if (o1.getUntil() == o2.getUntil()) {
                    return 0;
                } else {
                    return -1;
                }
            });
        }
        contentFiller.removeAllViews();
        for (Quest quest : toDisplay) {
            addCard(quest);
        }
    }

    /**
     * Adds a card to the content filler with information about a quest
     * @param quest the quest which information is displayed
     */
    public void addCard(Quest quest) {
        View card = inflater.inflate(R.layout.card_quest, null);
        String title = quest.getTitle();
        String description = quest.getDescription();
        Long time = quest.getRemainingTime();
        if (time <= 0) { return; }

        int score = quest instanceof XpQuest ? ((XpQuest) quest).getXp() : 0;
        String reward = quest instanceof RewardQuest ? ((RewardQuest) quest).getRewardDescription() : null;
        String color = quest.getColor();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.card_margin_b));
        card.setLayoutParams(params);
        card.setBackground(getCardBackground(color));
        card.setOnClickListener(getQuestListener(card));
        card.findViewById(R.id.view_in_map).setOnClickListener(getViewInMapListener(quest));
        card.findViewById(R.id.start_quest).setOnClickListener(getSelectQuestListener(quest));

        TextView titleField = card.findViewById(R.id.Title);
        TextView descField = card.findViewById(R.id.Description);
        TextView timeField = card.findViewById(R.id.Time);
        TextView scoreField = card.findViewById(R.id.Score);
        TextView scoreTitle = card.findViewById(R.id.score_title);
        ImageView scoreIcon = card.findViewById(R.id.score_icon);

        titleField.setText(title);
        descField.setText(description);
        timeField.setText(timePrettyPrint(time));
        if (reward != null) {
            scoreTitle.setText("Reward");
            scoreIcon.setImageResource(R.drawable.ic_outline_card_giftcard_24);
            scoreField.setText(reward);
        } else {
            scoreTitle.setText("XP");
            scoreIcon.setImageResource(R.drawable.ic_outline_arrow_circle_up_24);
            scoreField.setText(integerPrettyPrint(score));
        }

        contentFiller.addView(card);
    }

    /**
     * Gets the background drawable of the quest card
     * @param color identifying color of the quest
     * @return background drawable
     */
    public Drawable getCardBackground(String color) {
        LayerDrawable bg = (LayerDrawable) getContext().getDrawable(R.drawable.card_background);
        if (color != null) {
            float r = getResources().getDimensionPixelSize(R.dimen.card_border_radius);
            ShapeDrawable shape = new ShapeDrawable(new RoundRectShape(new float[]{r, r, r, r, r, r, r, r}, null, null));
            shape.getPaint().setColor(Color.parseColor(color));
            bg.setDrawableByLayerId(R.id.accentColor, shape);
        }
        return bg;
    }

    @Override
    public SwipeRefreshLayout.OnRefreshListener getRefreshListener() {
        return () -> Firestore.getAllQuests();
    }

    /**
     * @param quest quest object which is also the tag of its corresponding geomarker in the map
     * @return Listener that opens the map and select the corresponding geomarker of a quest
     */
    public View.OnClickListener getViewInMapListener(Quest quest) {
        return v -> ((MainActivity) getActivity()).openQuestInMap(quest);
    }

    /**
     * @param quest quest object which is to be selected
     * @return Listener that selects this quest as the on to be played
     */
    public View.OnClickListener getSelectQuestListener(Quest quest) {
        return v -> Firestore.checkIfTeamComplete(quest);
    }

    /**
     *
     * @param card Quest card which height is toggled and is scrolled to afterwards
     * @return Listener that toggles the displayed quest between contracted and expanded
     */
    public View.OnClickListener getQuestListener(View card) {
        return v -> {
            int descriptionHeightCon = getResources().getDimensionPixelSize(R.dimen.mid_description_height_con);
            int descriptionHeightExp = getResources().getDimensionPixelSize(R.dimen.mid_description_height_exp);
            int iconTitleHeightCon = getResources().getDimensionPixelSize(R.dimen.quest_icon_title_height_con);
            int iconTitleHeightExp = getResources().getDimensionPixelSize(R.dimen.quest_icon_title_height_exp);
            int namePaddingTBCon = getResources().getDimensionPixelSize(R.dimen.quest_name_padding_tb_con);
            int namePaddingTBExp = getResources().getDimensionPixelSize(R.dimen.quest_name_padding_tb_exp);
            int buttonsCon = getResources().getDimensionPixelSize(R.dimen.quest_buttons_height_con);
            int buttonsExp = getResources().getDimensionPixelSize(R.dimen.quest_buttons_height_exp);

            for (int i = 0; i < contentFiller.getChildCount(); i++) {
                View tmpCard = contentFiller.getChildAt(i);
                TextView description = tmpCard.findViewById(R.id.Description);
                TextView timeEmpty = tmpCard.findViewById(R.id.time_title);
                TextView scoreEmpty = tmpCard.findViewById(R.id.score_title);
                TextView titleTop = tmpCard.findViewById(R.id.TitleTop);
                TextView titleBottom = tmpCard.findViewById(R.id.TitleBottom);
                LinearLayout buttonLayout = tmpCard.findViewById(R.id.button_container);
                if (card.equals(tmpCard) && titleTop.getLayoutParams().height == namePaddingTBCon) {
                    animHeight(description, descriptionHeightCon, descriptionHeightExp, null);
                    animHeight(timeEmpty, iconTitleHeightCon, iconTitleHeightExp, null);
                    animHeight(scoreEmpty, iconTitleHeightCon, iconTitleHeightExp, null);
                    animHeight(titleTop, namePaddingTBCon, namePaddingTBExp, null);
                    animHeight(titleBottom, namePaddingTBCon, namePaddingTBExp, null);
                    animHeight(buttonLayout, buttonsCon, buttonsExp, card);
                } else {
                    animHeight(description, descriptionHeightExp, descriptionHeightCon, null);
                    animHeight(timeEmpty, iconTitleHeightExp, iconTitleHeightCon, null);
                    animHeight(scoreEmpty, iconTitleHeightExp, iconTitleHeightCon, null);
                    animHeight(titleTop, namePaddingTBExp, namePaddingTBCon, null);
                    animHeight(titleBottom, namePaddingTBExp, namePaddingTBCon, null);
                    animHeight(buttonLayout, buttonsExp, buttonsCon, card);
                }
            }
        };
    }

}
