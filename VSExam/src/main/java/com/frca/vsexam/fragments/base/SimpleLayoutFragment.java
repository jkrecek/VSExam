package com.frca.vsexam.fragments.base;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SimpleLayoutFragment extends Fragment {

    protected static final String EXTRA_RESOURCE_ID = "id";

    public static SimpleLayoutFragment newInstance(@LayoutRes int layoutId) {
        SimpleLayoutFragment f = new SimpleLayoutFragment();
        Bundle bdl = new Bundle();
        bdl.putInt(EXTRA_RESOURCE_ID, layoutId);
        f.setArguments(bdl);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int resourceId = getArguments().getInt(EXTRA_RESOURCE_ID);
        return inflater.inflate(resourceId, container, false);
    }
}
