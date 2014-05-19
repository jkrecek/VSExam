package com.frca.vsexam.fragments.exam_detail;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.frca.vsexam.R;
import com.frca.vsexam.entities.exam.Exam;
import com.frca.vsexam.helper.DataHolder;
import com.frca.vsexam.helper.Helper;
import com.frca.vsexam.network.HttpRequest;
import com.frca.vsexam.network.Response;
import com.frca.vsexam.network.tasks.BaseNetworkTask;
import com.frca.vsexam.network.tasks.TextNetworkTask;

import org.apache.http.client.methods.HttpRequestBase;

public class ExamButtonProvider extends DetailFragment.BaseExamProvider {

    public ExamButtonProvider(DetailFragment baseFragment, ViewGroup parent, LayoutInflater inflater) {
        super(baseFragment, parent, inflater, R.layout.exam_detail_buttons);
    }

    @Override
    public Result doLoad() {
        Button button_left = (Button) findViewById(R.id.button_left);
        Button button_right = (Button) findViewById(R.id.button_right);

        long currentServerTime = DataHolder.getInstance(getContext()).getNetworkInterface().getCurrentServerTime();
        long timeUntilRegistration = mExam.getRegisterStart().getTime() - currentServerTime;

        if (mExam.getGroup() == Exam.Group.TO_BE_REGISTERED)
            setButton(button_left, new OnCancelRegisterASAPClick(), true);
        else
            setButton(button_left, new OnRegisterASAPClick(), timeUntilRegistration > 0);

        if (timeUntilRegistration > 0) {

            boolean registrationSoon = timeUntilRegistration - (60 * 1000) < 0;
            setButton(button_right, new OnRegisterClick(), registrationSoon);

            if (!registrationSoon) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        load();
                    }
                }, timeUntilRegistration - (30 * 1000));
            }
        } else {
            if (!mExam.isRegistered())
                setButton(button_right, new OnRegisterClick(), true);
            else
                setButton(button_right, new OnUnregisterClick(), true);
        }

        return Result.DONE;
    }

    private void setButton(Button button, BaseOnClick onClick, boolean enabled) {
        button.setText(onClick.getTextResource());
        if (enabled)
            button.setOnClickListener(onClick);
        else
            button.setOnClickListener(null);

        button.setEnabled(enabled);
    }

    private abstract class BaseOnClick implements View.OnClickListener {

        abstract void onClick();
        abstract int getTextResource();

        @Override
        public void onClick(View view) {
            onClick();
        }
    }

    private class OnRegisterClick extends BaseOnClick {

        @Override
        public void onClick() {
            HttpRequestBase requestBase = HttpRequest.getRegisterRequest(DataHolder.getInstance(getContext()), mExam, true);
            final ProgressDialog dialog = ProgressDialog.show(getContext(), getContext().getString(R.string.register), getContext().getString(R.string.in_progress));

            BaseNetworkTask.run(new TextNetworkTask(getContext(), requestBase, new BaseNetworkTask.ResponseCallback() {
                @Override
                public void onSuccess(Response response) {
                    if (getMainFragment().getExams().onRegistrationResponse(getContext(), mExam, response)) {
                        Toast.makeText(getContext(), R.string.register_success, Toast.LENGTH_LONG).show();
                        getMainFragment().updateView();
                        dialog.dismiss();
                        Helper.appendLog("Register successful.");
                    } else {
                        Toast.makeText(getContext(), R.string.register_failure, Toast.LENGTH_LONG).show();
                        Helper.appendLog("Register unsuccessful.");
                    }

                }
            }));
        }

        @Override
        int getTextResource() {
            return R.string.register;
        }
    }

    private class OnUnregisterClick extends BaseOnClick {

        @Override
        public void onClick() {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

            builder.setTitle(getTextResource())
                .setMessage(R.string.unregister_confirmation)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        runUnregistering();
                        dialogInterface.dismiss();
                    }
                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });

            builder.create().show();
        }

        private void runUnregistering() {
            HttpRequestBase requestBase = HttpRequest.getRegisterRequest(DataHolder.getInstance(getContext()), mExam, false);
            final ProgressDialog dialog = ProgressDialog.show(getContext(), getContext().getString(R.string.register), getContext().getString(R.string.in_progress));
            BaseNetworkTask.run(new TextNetworkTask(getContext(), requestBase, new BaseNetworkTask.ResponseCallback() {
                @Override
                public void onSuccess(Response response) {
                    if (getMainFragment().getExams().onUnregistrationResponse(getContext(), mExam, response)) {
                        Toast.makeText(getContext(), R.string.unregister_success, Toast.LENGTH_LONG).show();
                        getMainFragment().updateView();
                        dialog.dismiss();
                        Helper.appendLog("Unregister successful.");
                    } else {
                        Toast.makeText(getContext(), R.string.unregister_failure, Toast.LENGTH_LONG).show();
                        Helper.appendLog("Unregister unsuccessful.");
                    }
                }
            }));
        }

        @Override
        int getTextResource() {
            return R.string.unregister;
        }
    }

    private class OnRegisterASAPClick extends BaseOnClick {

        @Override
        public void onClick() {
            Helper.appendLog("User clicked onto register on time");
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

            builder.setTitle(getTextResource())
                .setMessage(R.string.apply_rot_confirmation)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mExam.setToBeRegistered(getContext(), true);
                        load();
                        dialogInterface.dismiss();
                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });

            builder.create().show();
        }

        @Override
        int getTextResource() {
            return R.string.register_asap;
        }
    }

    private class OnCancelRegisterASAPClick extends BaseOnClick {

        @Override
        public void onClick() {
            Helper.appendLog("User clicked onto cancel register on time");
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

            builder.setTitle(getTextResource())
                .setMessage(R.string.cancel_rot_confirmation)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mExam.setToBeRegistered(getContext(), false);
                        dialogInterface.dismiss();
                        load();
                    }
                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });

            builder.create().show();
        }

        @Override
        int getTextResource() {
            return R.string.cancel_register_asap;
        }
    }
}
