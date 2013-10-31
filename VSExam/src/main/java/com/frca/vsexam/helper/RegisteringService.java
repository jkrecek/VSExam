package com.frca.vsexam.helper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.frca.vsexam.entities.base.Exam;
import com.frca.vsexam.network.HttpRequestBuilder;
import com.frca.vsexam.network.NetworkInterface;
import com.frca.vsexam.network.Response;
import com.frca.vsexam.network.tasks.BaseNetworkTask;
import com.frca.vsexam.network.tasks.TextNetworkTask;

import org.apache.http.client.methods.HttpRequestBase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class RegisteringService extends Service implements BaseNetworkTask.ResponseCallback {

    public static final String EXTRA_ID = "id";
    public static final String EXTRA_PERFORM_NOW = "perform_now";

    public static final int REQUEST_TIME_DIFF_MS = 100;

    private Exam exam;
    private DataHolder dataHolder;
    private HttpRequestBase registerRequest;

    private Thread thread;

    private List<TextNetworkTask> tasks = new ArrayList<TextNetworkTask>();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int examId = intent.getIntExtra(EXTRA_ID, 0);
        if (examId != 0) {
            exam = (Exam) Exam.getFromFile(Exam.class, this, examId);
        }

        if (exam.isRegistered()) {
            Log.e(getClass().getName(), "Exam is already registered");
        }
        dataHolder = DataHolder.getInstance(this);
        registerRequest = HttpRequestBuilder.getRegisterRequest(dataHolder, exam, true);
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    runRegistering();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

        return START_STICKY;
    }

    private void runRegistering() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        HttpRequestBase request = HttpRequestBuilder.getRegisterRequest(dataHolder, exam, true);
        Response response = dataHolder.getNetworkInterface().execute(request, Response.Type.TEXT);
        long serverTime = response.getServerTime().getTime();
        long endTime = System.currentTimeMillis();
        long responseLength = endTime - startTime;
        long serverTimeToReg = exam.getRegisterStart().getTime() - serverTime;
        if (serverTimeToReg < 0) {
            // already passed .... FCUK!
        }

        long timeToReg = serverTimeToReg - responseLength;
        if (timeToReg > 5000) {
            Thread.sleep(timeToReg - 5000);
        }

        // now spam registering
        while(!exam.isRegistered()) {
            TextNetworkTask task = new TextNetworkTask(this, registerRequest, this);
            tasks.add(task);
            BaseNetworkTask.run(task);
            Thread.sleep(REQUEST_TIME_DIFF_MS);
        }

    }

    private void onRegistered() {
        exam.setRegistered();
        for (TextNetworkTask task : tasks) {
            if (task.getStatus() != AsyncTask.Status.FINISHED)
                task.cancel(true);
        }

        stopSelf();
    }

    @Override
    public void onSuccess(Response response) {
        // lets consider it for the time being as a way to know we were registered successfully
        if (response.getStatusCode() == 302) {
            onRegistered();
            return;
        }

        // TODO: find a way to properly detect wrong register

    }

    public static AlarmManager getAlarmManager(Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public static void setExamRegister(Context context, Exam exam) {
        NetworkInterface networkInterface = DataHolder.getInstance(context).getNetworkInterface();
        long startInMillis = exam.getRegisterStart().getTime() - networkInterface.getCurrentServerTime();
        if (startInMillis < 0) {                    // should have already started
            // DAMN! What to do now?!
            sendPendingIntent(getAproxRegisterPI(context, exam, true));
        } else if (startInMillis < 30 * 1000) {     // starts in 30 seconds, handle right away
            sendPendingIntent(getAproxRegisterPI(context, exam, false));
        } else {
            startInMillis -= 30 * 1000;
            getAlarmManager(context).set(AlarmManager.RTC_WAKEUP, startInMillis, getAproxRegisterPI(context, exam, false));
        }
    }

    public static void cancelExamRegister(Context context, Exam exam) {
        PendingIntent pendingIntent = getAproxRegisterPI(context, exam, exam.getRegisterStart().before(new Date()));
        getAlarmManager(context).cancel(pendingIntent);
    }

    private static void sendPendingIntent(PendingIntent pendingIntent) {
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    private static PendingIntent getAproxRegisterPI(Context context, Exam exam, boolean performNow) {
        Intent intent = new Intent(context, RegisteringService.class);
        intent.putExtra(RegisteringService.EXTRA_ID, exam.getId());
        intent.putExtra(RegisteringService.EXTRA_PERFORM_NOW, performNow);
        return PendingIntent.getService(context, exam.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
