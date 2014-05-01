package com.frca.vsexam.helper;

public class MinimalMax {
    private int mMinimalMax;
    private int mRealMax;

    public MinimalMax(int minimalMax) {
        mMinimalMax = minimalMax;
        mRealMax = 0;
    }

    public int incrementMax() {
        ++mRealMax;
        return getMax();
    }

    public int getMax() {
        return Math.max(mMinimalMax, mRealMax);
    }
}
