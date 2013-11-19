package com.frca.vsexam.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.frca.vsexam.context.MainActivity;

/**
 * Created by KillerFrca on 14.10.13.
 */
public abstract class BaseFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);

        if (!(getActivity() instanceof MainActivity)) {
            Log.e(getClass().getName(), "This class must be child of MainActivity");
            getActivity().finish();
        }
    }

    public boolean onBackPressed() {
        return false;
    }

    protected MainActivity getMainActivity() {
        return (MainActivity)getActivity();
    }

    public void onNavigationItemSelected(int id) {

    }
}
