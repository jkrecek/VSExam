package com.frca.vsexam.fragments.base;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.frca.vsexam.context.MainActivity;

import java.util.Calendar;

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

    public void onNavigationItemSelected(final int id) {

    }
}
