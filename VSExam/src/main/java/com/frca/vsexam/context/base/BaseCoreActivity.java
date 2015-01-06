package com.frca.vsexam.context.base;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SlidingPaneLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.frca.vsexam.R;
import com.frca.vsexam.context.SettingsActivity;
import com.frca.vsexam.context.StartingActivity;
import com.frca.vsexam.entities.exam.ExamList;


public abstract class BaseCoreActivity extends BaseActivity {

    private static final int SETTINGS_CODE = 10;

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
                //SlidingPaneLayout slidingPaneLayout = getSlidingLayout();
                //if (slidingPaneLayout.isSlideable()) {
                    onBackPressed();
                    return true;
                //}
            }
            case R.id.action_refresh: {
                handleRefreshRequest();
                return true;
            }
            case R.id.action_settings: {
                startActivityForResult(new Intent(this, SettingsActivity.class), SETTINGS_CODE);
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    protected abstract void handleRefreshRequest();


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SETTINGS_CODE && resultCode == 1) {
            startActivity(new Intent(this, StartingActivity.class));
            finish();
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
