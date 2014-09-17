package com.frca.vsexam.views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class StartingViewPager extends ViewPager {

    public static final int SLIDE_NONE = 0;
    public static final int SLIDE_LEFT = 1;
    public static final int SLIDE_RIGHT = 2;

    private float startMoveX;
    private int mAllowedSlideSide = SLIDE_LEFT | SLIDE_RIGHT;

    public StartingViewPager(Context context) {
        super(context);

        init();
    }

    public StartingViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init() {
        setPageTransformer(true, new DepthPageTransformer());

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return !ignoreEvent(event) && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return !ignoreEvent(event) && super.onInterceptTouchEvent(event);
    }

    private boolean ignoreEvent(MotionEvent event) {
        int direction = getDirection(event);
        return direction != 0 && (direction & mAllowedSlideSide) == 0;
    }

    private int getDirection(MotionEvent event) {
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startMoveX = event.getX();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_MOVE: {
                float newX = event.getX();
                float dx = newX - startMoveX;

                if (dx > 0)
                    return SLIDE_RIGHT;
                else
                    return SLIDE_LEFT;
            }
        }

        return SLIDE_NONE;
    }

    public void setAllowedSlideSide(int allowedSlideSide) {
        mAllowedSlideSide = allowedSlideSide;
    }

    public class DepthPageTransformer implements PageTransformer {
        private static final float MIN_SCALE = 0.5f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(1);
                view.setScaleY(1);

            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }
}
