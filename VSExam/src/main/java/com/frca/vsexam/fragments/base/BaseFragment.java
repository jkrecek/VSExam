package com.frca.vsexam.fragments.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.frca.vsexam.context.BaseActivity;

public abstract class BaseFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);

        if (!(getActivity() instanceof BaseActivity)) {
            Log.e(getClass().getName(), "This class must be child of BaseActivity");
            getActivity().finish();
        }
    }

    public boolean onBackPressed() {
        return false;
    }

    protected BaseActivity getParentActivity() {
        return (BaseActivity)getActivity();
    }

    public void onNavigationItemSelected(final int id) {

    }
}
