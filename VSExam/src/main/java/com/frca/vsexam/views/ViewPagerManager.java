package com.frca.vsexam.views;

import android.support.v4.view.ViewPager;

import com.frca.vsexam.fragments.base.PagerFragment;

public class ViewPagerManager implements ViewPager.OnPageChangeListener {

    private ViewPager mPager;
    private ViewPagerIndicator mPagerIndicator;
    private ViewPager.OnPageChangeListener mOnPageChangeListener;
    private int mCurrentPage;

    public ViewPagerManager(ViewPager pager, ViewPagerIndicator indicator) {
        mPager = pager;
        mPagerIndicator = indicator;

        mPagerIndicator.setPager(mPager);
        mPager.setOnPageChangeListener(this);
        mCurrentPage = mPager.getCurrentItem();
    }

    public void setOnPageChangeListener(ViewPager.OnPageChangeListener onPageChangeListener) {
        this.mOnPageChangeListener = onPageChangeListener;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (this.mOnPageChangeListener != null) {
            this.mOnPageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }
    }

    @Override
    public void onPageSelected(final int position) {
        Object o = mPager.getAdapter().instantiateItem(mPager, mCurrentPage);
        if (o instanceof PagerFragment) {
            ((PagerFragment) o).postInactive();
        }
        o = mPager.getAdapter().instantiateItem(mPager, position);
        if (o instanceof PagerFragment) {
            ((PagerFragment) o).postActive();
        }

        mCurrentPage = position;
        mPagerIndicator.notifyDataSetChanged();
        if (this.mOnPageChangeListener != null) {
            this.mOnPageChangeListener.onPageSelected(position);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (this.mOnPageChangeListener != null) {
            this.mOnPageChangeListener.onPageScrollStateChanged(state);
        }
    }
}
