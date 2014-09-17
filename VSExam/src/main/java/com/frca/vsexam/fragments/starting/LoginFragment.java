package com.frca.vsexam.fragments.starting;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.frca.vsexam.R;
import com.frca.vsexam.context.BaseActivity;
import com.frca.vsexam.context.StartingActivity;
import com.frca.vsexam.fragments.base.BaseFragment;
import com.frca.vsexam.helper.DataHolder;
import com.frca.vsexam.network.HttpRequestBuilder;

public class LoginFragment extends BaseFragment implements View.OnClickListener {

    private TextView mViewLogin;

    private TextView mViewPassword;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_starting_login, container, false);

        mViewLogin = (TextView) rootView.findViewById(R.id.text_login);
        mViewPassword = (TextView) rootView.findViewById(R.id.text_password);
        //rootView.findViewById(R.id.image_logo).setVisibility(View.GONE);

        Button button = (Button)rootView.findViewById(R.id.button);
        button.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View view) {
        String loginInput = mViewLogin.getText().toString();
        String loginPassword = mViewPassword.getText().toString();

        if (TextUtils.isEmpty(loginInput))
            mViewLogin.setError(getString(R.string.login_condition_xname));

        if (TextUtils.isEmpty(loginPassword))
            mViewPassword.setError(getString(R.string.login_condition_password));

        if (mViewLogin.getError() != null)
            mViewLogin.requestFocus();
        else if (mViewPassword.getError() != null)
            mViewPassword.requestFocus();
        else {
            if (getActivity() instanceof BaseActivity) {
                SharedPreferences preferences = DataHolder.getInstance(getActivity()).getPreferences();

                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mViewPassword.getWindowToken(), 0);

                String loginPasswordKey = loginInput + ":" + loginPassword;
                String authKey = Base64.encodeToString(loginPasswordKey.getBytes(), Base64.URL_SAFE | Base64.NO_WRAP);
                preferences
                    .edit()
                    .putString(HttpRequestBuilder.KEY_AUTH_KEY, authKey)
                    .commit();

                getStartingActivity().startExamLoading();
                //dismiss();
            }
        }
    }

    private StartingActivity getStartingActivity() {
        return (StartingActivity) getActivity();
    }
}
