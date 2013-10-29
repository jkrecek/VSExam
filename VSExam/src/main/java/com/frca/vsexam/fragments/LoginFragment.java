package com.frca.vsexam.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.frca.vsexam.R;
import com.frca.vsexam.context.MainActivity;
import com.frca.vsexam.helper.DataHolder;
import com.frca.vsexam.network.HttpRequestBuilder;

/**
 * Created by KillerFrca on 11.10.13.
 */
public class LoginFragment extends BaseFragment {
    public LoginFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        final TextView viewLogin = (TextView) rootView.findViewById(R.id.text_login);
        final TextView viewPassword = (TextView) rootView.findViewById(R.id.text_password);

        Button button = (Button)rootView.findViewById(R.id.sign_in_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String loginInput = viewLogin.getText().toString();
                String loginPassword = viewPassword.getText().toString();

                if (TextUtils.isEmpty(loginInput))
                    viewLogin.setError("You must input your xname");

                if (TextUtils.isEmpty(loginPassword))
                    viewPassword.setError("You must input your password");

                if (viewLogin.getError() != null)
                    viewLogin.requestFocus();
                else if (viewPassword.getError() != null)
                    viewPassword.requestFocus();
                else {
                    if (getActivity() instanceof MainActivity) {
                        SharedPreferences preferences = DataHolder.getInstance(getActivity()).getPreferences();

                        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(viewPassword.getWindowToken(), 0);

                        preferences
                            .edit()
                            .putString(HttpRequestBuilder.KEY_LOGIN, loginInput)
                            .putString(HttpRequestBuilder.KEY_PASSWORD, loginPassword)
                            .commit();

                        ((MainActivity)getActivity()).setFragment(new LoadingFragment(null));
                    }
                }
            }
        });

        return rootView;
    }
}
