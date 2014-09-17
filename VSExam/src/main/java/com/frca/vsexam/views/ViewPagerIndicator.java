package com.frca.vsexam.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.frca.vsexam.R;

public class ViewPagerIndicator extends LinearLayout{
    private Context mContext;
    private ViewPager mPager;
    private int mImageMargin;

    public ViewPagerIndicator(Context context) {
        super(context);
        this.mContext = context;

        init();
    }

    public ViewPagerIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;

        init();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public ViewPagerIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;

        init();
    }

    private void init() {
        mImageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics());
    }

    public void setPager(ViewPager pager) {
        mPager = pager;
        notifyDataSetChanged();
    }

    public void setImageMargin(int dpMargin) {
        mImageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpMargin, getResources().getDisplayMetrics());
    }

    public void notifyDataSetChanged() {
        int count = mPager.getAdapter().getCount();

        for (int i = 0; i < count; i++) {
            ImageView item = (ImageView) getChildAt(i);
            if (item == null)
                item = createNewChild(i);

            if (i == mPager.getCurrentItem())
                item.setImageResource(R.drawable.pager_indicator_selected);
            else
                item.setImageResource(R.drawable.pager_indicator_unselected);
        }
    }

    private ImageView createNewChild(final int position) {
        ImageView item = new ImageView(mContext);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(mImageMargin, mImageMargin, mImageMargin, mImageMargin);
        item.setLayoutParams(lp);

        /*item.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPager.setCurrentItem(position);
            }
        });*/

        addView(item);
        return item;
    }
}
