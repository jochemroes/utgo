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
import nl.utwente.utgo.content.QuestsContent;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link QuestsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class QuestsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    //the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "QUESTS";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private PagerAdapter adapter;
    private ViewPager2 viewPager;

    public QuestsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Quests.
     */
    // TODO: Rename and change types and number of parameters
    public static QuestsFragment newInstance(String param1, String param2) {
        QuestsFragment fragment = new QuestsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Loads the content in all of its content fragments
     */
    public void loadAllTabs() {
        for (int i = 0; i < adapter.getFragmentCount(); i++) {
            adapter.getFragment(i).loadContent();
        }
    }

    /**
     * Stops refreshing in all of its content fragments
     */
    public void stopRefreshing() {
        for (int i = 0; i < adapter.getFragmentCount(); i++) {
            adapter.getFragment(i).stopRefreshing();
        }
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
        return inflater.inflate(R.layout.fragment_quests, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        String[] tabNames = {"All", "XP", "Reward"};
        adapter = new PagerAdapter(this, 3, PagerAdapter.ContentType.QUESTS);
        viewPager = view.findViewById(R.id.quests_view_pager);
        viewPager.setAdapter(adapter);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position < adapter.getFragmentCount())
                    ((QuestsContent) adapter.getFragment(position)).changeTitleBar();
            }
        });
        TabLayout tabLayout = view.findViewById(R.id.quests_tab_layout);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    tab.setText(tabNames[position]);
                    tab.view.setClickable(false);
                }
        ).attach();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            ((QuestsContent) adapter.getFragment(viewPager.getCurrentItem())).changeTitleBar();
        }
    }


}