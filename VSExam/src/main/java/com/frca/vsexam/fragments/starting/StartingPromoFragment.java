package com.frca.vsexam.fragments.starting;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.frca.vsexam.R;
import com.frca.vsexam.context.StartingActivity;
import com.frca.vsexam.fragments.base.SimpleLayoutFragment;

public class StartingPromoFragment extends SimpleLayoutFragment {

    public static StartingPromoFragment newInstance(@LayoutRes int layoutId) {
        StartingPromoFragment f = new StartingPromoFragment();
        Bundle bdl = new Bundle();
        bdl.putInt(EXTRA_RESOURCE_ID, layoutId);
        f.setArguments(bdl);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        Button b = (Button) root.findViewById(R.id.button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewPager pager = ((StartingActivity) getActivity()).getPager();
                pager.setCurrentItem(pager.getCurrentItem() + 1, true);
            }
        });

        return root;
    }
}
