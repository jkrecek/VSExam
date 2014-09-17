package com.frca.vsexam.adapters;

import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.frca.vsexam.R;
import com.frca.vsexam.fragments.starting.LoadingFragment;
import com.frca.vsexam.fragments.starting.LoginFragment;
import com.frca.vsexam.fragments.starting.StartingPromoFragment;

public class BasePagerAdapter extends FragmentStatePagerAdapter {

    public static final int PAGE_COUNT = 4;

    @LayoutRes
    private static int[] sSimpleLayouts = new int[] { R.layout.fragment_starting_promo_1, R.layout.fragment_starting_promo_2};

    public BasePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
            case 1:
                return StartingPromoFragment.newInstance(sSimpleLayouts[position]);
            case 2:
                return new LoginFragment();
            case 3:
                return new LoadingFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

}
