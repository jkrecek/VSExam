package com.frca.vsexam.fragments.starting;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.frca.vsexam.R;
import com.frca.vsexam.context.BaseActivity;
import com.frca.vsexam.context.StartingActivity;
import com.frca.vsexam.entities.exam.ExamList;
import com.frca.vsexam.fragments.base.PagerFragment;
import com.frca.vsexam.network.tasks.TextNetworkTask;

public class LoadingFragment extends PagerFragment {

    private TextNetworkTask mNetworkTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_starting_loading, container, false);
    }

    @Override
    public void onActive() {
        doStartLoad();
    }

    @Override
    public void onInactive() {
        if (mNetworkTask != null && mNetworkTask.getStatus() == AsyncTask.Status.RUNNING)
            mNetworkTask.cancel(true);
    }

    private void doStartLoad() {
        mNetworkTask = getParentActivity().loadExams(new BaseActivity.LoadingExamResult() {
            @Override
            public void onExamLoadingSuccess(ExamList exams) {
                getStartingActivity().startMainActivity(exams);
                mNetworkTask = null;
            }

            @Override
            public void onExamLoadingDenied() {
                getStartingActivity().requestCredentials();
                mNetworkTask = null;
            }

            @Override
            public void onExamLoadingError() {
                doStartLoad();
                mNetworkTask = null;
            }
        });
    }

    private StartingActivity getStartingActivity() {
        return (StartingActivity) getActivity();
    }
}
