package com.frca.vsexam.context;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.frca.vsexam.R;
import com.frca.vsexam.context.base.BaseCoreActivity;

public class ExamlessActivity extends BaseCoreActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_examless);
    }


    @Override
    protected void handleRefreshRequest() {
        startActivity(new Intent(this, StartingActivity.class));
    }
}
