package com.frca.vsexam.context;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import com.frca.vsexam.R;
import com.frca.vsexam.entities.exam.ExamList;
import com.frca.vsexam.fragments.MainFragment;
import com.frca.vsexam.fragments.LoadingFragment;
import com.frca.vsexam.fragments.LoginFragment;
import com.frca.vsexam.fragments.base.BaseFragment;
import com.frca.vsexam.helper.AppConfig;
import com.frca.vsexam.helper.DataHolder;
import com.frca.vsexam.helper.Utils;
import com.frca.vsexam.network.HttpRequestBuilder;

import java.util.List;

public class MainActivity extends ActionBarActivity {

    private BaseFragment currentFragment;

    private ExamList mExams;

    private static MainActivity instance = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;

        setContentView(R.layout.activity_main);

        if (AppConfig.LAUNCH_ON_START != null) {
            if (Activity.class.isAssignableFrom(AppConfig.LAUNCH_ON_START)) {
                startActivity(new Intent(this, AppConfig.LAUNCH_ON_START));

            } else if (BaseFragment.class.isAssignableFrom(AppConfig.LAUNCH_ON_START)) {
                try {
                    setFragment((BaseFragment) AppConfig.LAUNCH_ON_START.newInstance());
                } catch (Exception e) { /* just ignore */ }

            }
        } else if (hasSavedLoginData()) {
            setFragment(new LoadingFragment());
        } else {
            setFragment(new LoginFragment());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        instance = this;

        if (!hasSavedLoginData() && (currentFragment == null || !(currentFragment instanceof LoginFragment))) {
            setFragment(new LoginFragment());
        } else {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container_base);
            if (fragment != null && !fragment.equals(currentFragment))
                setFragment(currentFragment);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        instance = null;
    }

    public void setFragment(BaseFragment fragment) {
        currentFragment = fragment;
        if (instance != null)
            getSupportFragmentManager().beginTransaction().replace(R.id.container_base, fragment).commit();

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
                if (currentFragment instanceof MainFragment) {
                    SlidingPaneLayout slidingPaneLayout = ((MainFragment) currentFragment).getSlidingLayout();
                    if (slidingPaneLayout.isSlideable()) {
                        onBackPressed();
                        return true;
                    }
                }
                break;
            }
            /*case R.id.action_json: {
                ExamList examList = getLoadedExams();
                if (examList == null) {
                    examList = new ExamList();
                    examList.loadSaved(this);
                }

                Gson gson = new Gson();
                StringBuilder sb = new StringBuilder();
                for (Exam exam : examList) {
                    sb.append(gson.toJson(exam));
                    sb.append("\n\n");
                }

                final String exam_json = sb.toString();

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.exam_output)
                    .setMessage(exam_json)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).setNegativeButton(R.string.into_file, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();

                            String result = Helper.writeToFile(exam_json,
                                Helper.getDataDirectoryFile("data", "output_" + String.valueOf(System.currentTimeMillis() / 1000L), "json"),
                                false);

                            if (result == null)
                                result = getString(R.string.successfully_saved_into_file);

                            Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();
                        }
                    }).create().show();
                break;
            }*/
            case R.id.action_refresh: {
                setFragment(new LoadingFragment());
                break;
            }
            case R.id.action_settings: {
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (currentFragment != null)
            if (currentFragment.onBackPressed())
                return;

        super.onBackPressed();
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void setActionBarAdapter(List<String> values) {
        ActionBar actionBar = getSupportActionBar();
        if (values == null) {
            actionBar.setDisplayShowTitleEnabled(true);
            getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        } else {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, values);
            actionBar.setListNavigationCallbacks(adapter, new ActionBarAdapterClickListener());
        }

    }

    private class ActionBarAdapterClickListener implements ActionBar.OnNavigationListener {

        @Override
        public boolean onNavigationItemSelected(int i, long l) {
            currentFragment.onNavigationItemSelected(i);
            return false;
        }
   }

    public static MainActivity getInstance() {
        return instance;
    }

    public static MainFragment getMainFragment() {
        if (instance != null && instance.currentFragment != null && instance.currentFragment instanceof MainFragment)
            return (MainFragment) instance.currentFragment;

        return null;
    }

    private boolean hasSavedLoginData() {
        return DataHolder.getInstance(this).getPreferences().contains(HttpRequestBuilder.KEY_AUTH_KEY);
    }

    public ExamList getExams() {
        return mExams;
    }

    public void setExams(ExamList exams) {
        mExams = exams;
    }
}
