package nl.utwente.utgo.content;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import nl.utwente.utgo.Firestore;
import nl.utwente.utgo.R;

public abstract class Content extends Fragment {

    protected LayoutInflater inflater;

    protected SwipeRefreshLayout swipeLayout;
    protected ScrollView scroll;
    protected LinearLayout contentFiller;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content, container, false);

        this.inflater = inflater;
        contentFiller = view.findViewById(R.id.content_filler);
        swipeLayout = view.findViewById(R.id.swipe_container);
        scroll = view.findViewById(R.id.scroll);

        swipeLayout.setOnRefreshListener(getRefreshListener());

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        loadContent();
    }

    /**
     * Fills the contentFiller view with relevant views
     * (for example Quest cards in the Quest view)
     */
    public void loadContent() {}

    /**
     * Changes the height of a view in a smooth way
     * @param view view of which the height will change
     * @param before initial height
     * @param after height-to-be
     * @param card card to scroll to after height change (needed if height change puts part of the card out of sight)
     */
    public void animHeight(View view, int before, int after, View card) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params.height == before) {
            ValueAnimator anim = ValueAnimator.ofInt(before, after);
            anim.addUpdateListener(valueAnimator -> {
                int val = (Integer) valueAnimator.getAnimatedValue();
                params.height = val;
                view.setLayoutParams(params);
            });
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (card != null) focusOnView(card);
                }
            });
            anim.setDuration(200);
            anim.start();
        }
    }

    /**
     * Scrolls to a view to ensure it is in sight
     * @param view view to be scrolled to
     */
    public void focusOnView(View view) {
        int currentPosition = scroll.getScrollY();
        int scrollHeight = scroll.getHeight();
        int viewTop = view.getTop();
        int viewBottom = view.getBottom();
        int topPadding = getResources().getDimensionPixelSize(R.dimen.scroll_padding_top);
        int bottomPadding = getResources().getDimensionPixelSize(R.dimen.scroll_padding_bottom);
        if (viewTop < currentPosition - topPadding) {
            scroll.post(() -> scroll.smoothScrollTo(0, viewTop - topPadding));
        }
        if (viewBottom > currentPosition + scrollHeight - bottomPadding) {
            scroll.post(() -> scroll.smoothScrollTo(0, viewBottom - scrollHeight + bottomPadding));
        }
    }

    /**
     * What function to call when the page is refreshed
     * @return the listener that calls the function
     */
    public SwipeRefreshLayout.OnRefreshListener getRefreshListener() { return () -> Firestore.getCollectionReferencesFromUser(); }

    /**
     * Stops the refreshing
     * Should be called after new data is retrieved
     */
    public void stopRefreshing() {
        if (swipeLayout != null) swipeLayout.setRefreshing(false);
    }

}
