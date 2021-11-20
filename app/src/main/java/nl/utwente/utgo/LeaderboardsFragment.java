package nl.utwente.utgo;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import nl.utwente.utgo.content.PagerAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LeaderboardsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LeaderboardsFragment extends Fragment {

    private static final String TAG = "Leaderboards Fragment";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private PagerAdapter adapter;
    private ViewPager2 viewPager;

    public LeaderboardsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Leaderboards.
     */
    // TODO: Rename and change types and number of parameters
    public static LeaderboardsFragment newInstance(String param1, String param2) {
        LeaderboardsFragment fragment = new LeaderboardsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_leaderboards, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        String[] tabNames = {"Players", "Teams", "Do-groups", "Assocations"};
        adapter = new PagerAdapter(this, 4, PagerAdapter.ContentType.LEADERBOARDS);
        viewPager = view.findViewById(R.id.leaderboards_view_pager);
        viewPager.setAdapter(adapter);
        TabLayout tabLayout = view.findViewById(R.id.leaderboards_tab_layout);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    tab.setText(tabNames[position]);
                    tab.view.setClickable(true);
                }
        ).attach();
    }

    /**
     * Loads the cards in all of its content fragments
     */
    public void loadAllTabs() {
        for (int i = 0; i < adapter.getFragmentCount(); i++) {
            adapter.getFragment(i).loadContent();
        }
    }

    /**
     * Stops possible refreshing in all of its content fragments
     */
    public void stopRefreshing() {
        for (int i = 0; i < adapter.getFragmentCount(); i++) {
            adapter.getFragment(i).stopRefreshing();
        }
    }

}