package nl.utwente.utgo.content;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public class PagerAdapter extends FragmentStateAdapter {

    public enum ContentType {QUESTS, LEADERBOARDS, PROFILE, SETTINGS}
    private ContentType contentType;
    private int itemCount;
    private List<Content> fragments = new ArrayList<>();

    public PagerAdapter(@NonNull Fragment fragment, int itemCountArg, ContentType contentTypeArg) {
        super(fragment);
        itemCount = itemCountArg;
        contentType = contentTypeArg;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Content fragment;
        switch (contentType) {
            case QUESTS : fragment = new QuestsContent(position); break;
            case LEADERBOARDS : fragment = new LeaderboardsContent(position); break;
            case PROFILE : fragment = new ProfileContent(); break;
            case SETTINGS : fragment = new SettingsContent(); break;
            default : fragment = null;
        }
        fragments.add(fragment);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return itemCount;
    }

    /**
     * @return The number of fragments
     */
    public int getFragmentCount() {
        return fragments.size();
    }

    /**
     * @param position Index of the desired fragment
     * @return Fragment at that index
     */
    public Content getFragment(int position) {
        return fragments.get(position);
    }
}
