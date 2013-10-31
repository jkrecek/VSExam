package com.frca.vsexam.context;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.frca.vsexam.R;
import com.frca.vsexam.entities.base.Exam;
import com.frca.vsexam.fragments.BaseFragment;
import com.frca.vsexam.fragments.BrowserPaneFragment;
import com.frca.vsexam.fragments.LoadingFragment;
import com.frca.vsexam.fragments.LoginFragment;
import com.frca.vsexam.helper.DataHolder;
import com.frca.vsexam.network.HttpRequestBuilder;
import com.google.gson.Gson;

public class MainActivity extends ActionBarActivity {

    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = DataHolder.getInstance(this).getPreferences();
        if (preferences.contains(HttpRequestBuilder.KEY_LOGIN) && preferences.contains(HttpRequestBuilder.KEY_PASSWORD)) {
            setFragment(new LoadingFragment("Preparing"));
        } else {
            setFragment(new LoginFragment());
        }
    }

    public void setFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.container_base, fragment).commit();
        currentFragment = fragment;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
        // TODO
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                if (currentFragment instanceof BrowserPaneFragment) {
                    SlidingPaneLayout slidingPaneLayout = ((BrowserPaneFragment) currentFragment).getSlidingLayout();
                    if (slidingPaneLayout.isSlideable()) {
                        onBackPressed();
                        return true;
                    }
                }
                break;
            }
            case R.id.action_settings: {
                Exam[] exams = OnStartReceiver.getSavedExams(this);
                Toast.makeText(this, String.valueOf(exams.length), Toast.LENGTH_LONG).show();
                Gson gson = new Gson();
                String total = "";
                for (Exam exam : exams) {
                    total += gson.toJson(exam) + "\n\n";
                }

                new AlertDialog.Builder(this).setTitle("Jsons").setMessage(total).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create().show();

                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (currentFragment instanceof BaseFragment)
            if (((BaseFragment)currentFragment).onBackPressed())
                return;

        super.onBackPressed();
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }
}
