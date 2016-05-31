package com.exolvetechnologies.hidoctor.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.exolvetechnologies.hidoctor.R;
import com.exolvetechnologies.hidoctor.views.SlidingTabLayout;

/**
 * Created by ekeretepeter on 08/04/16.
 */
public class MyBabyAndIFragment extends Fragment {

    static final String LOG_TAG = "SlidingTabsBasic";

    /**
     * A custom {@link ViewPager} title strip which looks much like Tabs present in Android v4.0 and
     * above, but is designed to give continuous feedback to the user when scrolling.
     */
    private SlidingTabLayout mSlidingTabLayout;

    /**
     * A {@link ViewPager} which will be used in conjunction with the {@link SlidingTabLayout} above.
     */
    private ViewPager mViewPager;
    View view;

    /**
     * Inflates the {@link View} which will be displayed by this {@link Fragment}, from the app's
     * resources.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_tabbed_home, container, false);

        return view;
    }

    // BEGIN_INCLUDE (fragment_onviewcreated)

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // BEGIN_INCLUDE (setup_viewpager)
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        //mViewPager.setAdapter(new SamplePagerAdapter());
        mViewPager.setAdapter(new AppSectionsPagerAdapter(getFragmentManager()));
        // END_INCLUDE (setup_viewpager)

        // BEGIN_INCLUDE (setup_slidingtablayout)
        // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.
        mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager);
        // END_INCLUDE (setup_slidingtablayout)
    }
    // END_INCLUDE (fragment_onviewcreated)


    public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {

            switch (i) {

                case 0:

                    return new ForumFragment();
                case 1:

                    return BlogCategoryFragment.newInstance("7");

                case 2:

                    return new PregnancyCareFragment();

                default:

                    return new PregnancyCareFragment();
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case 0:
                    return "Forum";
                case 1:
                    return "Blog";
                case 2:
                    return "Pregnancy: Week By Week";
                default:
                    return  "Section " + (position + 1);
            }

        }
    }
}
