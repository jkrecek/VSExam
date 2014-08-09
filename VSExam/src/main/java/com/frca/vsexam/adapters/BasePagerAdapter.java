package com.frca.vsexam.adapters;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

public class BasePagerAdapter extends PagerAdapter {

    private ViewPager mPager;

    private BasePagerAdapter(ViewPager pager) {
        mPager = pager;
    }

    public static BasePagerAdapter appendToPager(ViewPager pager) {
        BasePagerAdapter pagerAdapter = new BasePagerAdapter(pager);
        pager.setAdapter(pagerAdapter);
        return pagerAdapter;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        return mPager.getChildAt(position);
    }

    @Override
    public int getCount() {
        return mPager.getChildCount();
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return view == o;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

    }

}
