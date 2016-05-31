/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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


public class TabbedBlogListFragment extends Fragment {

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

                    return new AllBlogsFragment();
                case 1:

                    return BlogCategoryFragment.newInstance("1");

                case 2:

                    return BlogCategoryFragment.newInstance("2");

                case 3:

                    return BlogCategoryFragment.newInstance("3");

                case 4:

                    return BlogCategoryFragment.newInstance("4");

                case 5:

                    return BlogCategoryFragment.newInstance("5");

                case 6:

                    return BlogCategoryFragment.newInstance("7");

                default:

                    // The other sections of the app are dummy placeholders.
                    /*Fragment fragment = new DummySectionFragment();
                    Bundle args = new Bundle();
                    args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, i + 1);
                    fragment.setArguments(args);
                    return fragment;*/
                    return new AllBlogsFragment();
            }
        }

        @Override
        public int getCount() {
            return 7;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case 0:
                    return "All";
                case 1:
                    return "General Health";
                case 2:
                    return "Sex Clinic";
                case 3:
                    return "Dental Health";
                case 4:
                    return "Quick Facts";
                case 5:
                    return "Doctors' Diaries";
                case 6:
                    return "My Baby & I";
                default:
                    return  "Section " + (position + 1);
            }

        }
    }

    /*public static class DummySectionFragment extends Fragment {

        public static final String ARG_SECTION_NUMBER = "section_number";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_section_dummy, container, false);
            Bundle args = getArguments();
            ((TextView) rootView.findViewById(android.R.id.text1)).setText(
                    getString(R.string.dummy_section_text, args.getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }*/
}
