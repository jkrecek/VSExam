package com.frca.vsexam.context;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.view.MenuItem;
import android.widget.Toast;

import com.frca.vsexam.R;
import com.frca.vsexam.entities.vsedata.VSEStructure;
import com.frca.vsexam.entities.vsedata.VSEStructureParser;
import com.frca.vsexam.helper.Dialog;
import com.frca.vsexam.helper.MinimalMax;
import com.frca.vsexam.network.HttpRequestBuilder;
import com.frca.vsexam.network.tasks.BaseNetworkTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;

@SuppressWarnings( "deprecation" )
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            final ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }

        addPreferencesFromResource(R.xml.settings);

        SharedPreferences preferences = getBaseSharedPreferences();
        if (preferences != null) {
            for (String key : preferences.getAll().keySet()) {
                setValueAsSummary(preferences, key);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        getBaseSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        getBaseSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    public SharedPreferences getBaseSharedPreferences() {
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (preferenceScreen != null)
            return preferenceScreen.getSharedPreferences();

        return null;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, final Preference preference) {
        String key = preference.getKey();
        if (key != null &&preference.getClass() == Preference.class) {
            try {
                getClass()
                    .getMethod(key, Preference.class)
                    .invoke(this, preference);
                return true;
            } catch (ReflectiveOperationException e) {
                Toast.makeText(this, R.string.unknown_action, Toast.LENGTH_LONG).show();
                e.printStackTrace();
                return false;
            }
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        setValueAsSummary(sharedPreferences, key);
    }

    private void setValueAsSummary(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if (preference == null)
            return;

        String summary = null;

        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            preference.setSummary(listPreference.getEntry());
        } else if (preference instanceof EditTextPreference) {
            EditTextPreference editTextPreference = (EditTextPreference) preference;
            summary = editTextPreference.getText();

            int variation = editTextPreference.getEditText().getInputType() & InputType.TYPE_MASK_VARIATION;
            if (variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD) {
                summary = summary.replaceAll(".", "*");
            }
        } else if (preference.getClass() == Preference.class) {
            summary = sharedPreferences.getString(key, null);
        }

        if (summary != null)
            preference.setSummary(summary);
    }

    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return true;
    }

    public void handleStructureView(final Preference preference) {
        String structureString = VSEStructure.load(this).toPrettyJsonString();
        new AlertDialog.Builder(this)
            .setTitle(preference.getTitle())
            .setMessage(structureString)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            })
            .create()
            .show();
    }

    public void handleStructureRefresh(final Preference preference) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle(preference.getTitle());
        dialog.setMessage(getString(R.string.loading_with_ellipsis));
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setCancelable(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            dialog.setProgressNumberFormat(getString(R.string.progress_structure_number_format));
        dialog.show();

        final MinimalMax max = new MinimalMax(50);
        VSEStructureParser.loadData(this, new VSEStructureParser.OnLoadedCallback() {
            @Override
            public void loaded(VSEStructure vseStructure) {
                dialog.dismiss();

                vseStructure.save(SettingsActivity.this);

                Dialog.Ok(SettingsActivity.this, new Dialog.Callback() {
                    @Override
                    public void call(AlertDialog.Builder dialog) {
                        dialog
                            .setTitle(R.string.data_downloaded)
                            .setMessage(R.string.structure_downloaded_saved);
                    }
                });

                getBaseSharedPreferences()
                    .edit()
                    .putString(
                        preference.getKey(),
                        getString(R.string.last_update_on_x, new SimpleDateFormat("dd. MM. yyyy HH:mm:ss").format(Calendar.getInstance().getTime()))
                    )
                    .commit();


            }
        }, new VSEStructureParser.OnTaskStatusChanged() {
            @Override
            public void changed(BaseNetworkTask task, boolean added) {
                if (added)
                    dialog.setMax(max.incrementMax());
                else
                    dialog.incrementProgressBy(1);

            }
        });
    }

    public void handleLogOff(final Preference preference) {
        getBaseSharedPreferences()
            .edit()
            .remove(HttpRequestBuilder.KEY_LOGIN)
            .remove(HttpRequestBuilder.KEY_PASSWORD)
            .commit();

        finish();
    }


}