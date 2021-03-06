package com.example.cameron.ethereumtest1.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.example.cameron.ethereumtest1.fragments.UserContentRecyclerViewFragment;
import com.example.cameron.ethereumtest1.fragments.UserPublicationsRecyclerViewFragment;

public class UserFragmentPagerAdapter extends FragmentPagerAdapter {

    String mSelectedAddress;

    public UserFragmentPagerAdapter(FragmentManager fm, String selectedAddress) {
        super(fm);
        mSelectedAddress = selectedAddress;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return UserContentRecyclerViewFragment.newInstance(mSelectedAddress);
            default:
            case 1:
                return UserPublicationsRecyclerViewFragment.newInstance(mSelectedAddress);
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public int getItemPosition(Object object) {
        if (object instanceof UserContentRecyclerViewFragment) {
            ((UserContentRecyclerViewFragment)object).setSelectedAddress(mSelectedAddress);
            ((UserContentRecyclerViewFragment)object).loadContentList();
        }

        return POSITION_NONE;
    }
}
