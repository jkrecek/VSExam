package com.frca.vsexam.helper;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.SparseArray;
import android.widget.Toast;

import com.frca.vsexam.R;
import com.frca.vsexam.context.MainActivity;
import com.frca.vsexam.entities.exam.Exam;
import com.frca.vsexam.entities.exam.ExamList;
import com.frca.vsexam.network.HttpRequestBuilder;
import com.frca.vsexam.network.NetworkInterface;
import com.frca.vsexam.network.Response;
import com.frca.vsexam.network.tasks.BaseNetworkTask;
import com.frca.vsexam.network.tasks.TextNetworkTask;

import org.apache.http.client.methods.HttpRequestBase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class RegisteringService extends Service {

    public static final String EXTRA_ID = "id";
    public static final String EXTRA_PERFORM_NOW = "perform_now";

    public static final int REQUEST_TIME_DIFF_MS = 300;

    private Exam exam;
    private ExamList examList;
    private DataHolder dataHolder;

    private Thread thread;

    private List<TextNetworkTask> tasks = new ArrayList<TextNetworkTask>();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int examId = intent.getIntExtra(EXTRA_ID, 0);
        Helper.appendLog("Registering process starting");
        examList = MainActivity.getLoadedExams();
        if (examList == null)
            examList = new ExamList();

        if (examId != 0)
            exam = examList.load(this, examId);

        if (exam == null) {
            Helper.appendLog("ERROR: Exam data could not be found nor loaded from file!");
            stopSelf();
        } else if (exam.isRegistered()) {
            Helper.appendLog("ERROR: Exam is already registered!");
            stopSelf();
        } else {
            dataHolder = DataHolder.getInstance(this);
            SparseArray<RegisteringService> container = dataHolder.getRegisteringServiceContainer();
            synchronized (container) {
                RegisteringService runningService = container.get(exam.getId());
                if (runningService != null) {
                    Helper.appendLog("ERROR: Registering service is already running!");
                    stopSelf();
                    return START_STICKY;
                } else {
                    container.put(exam.getId(), this);
                }
            }
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
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Helper.appendLog("Registering service stopped, canceling concurrent tasks");
        for (TextNetworkTask task : tasks) {
            if (task != null && task.getStatus() != AsyncTask.Status.FINISHED)
                task.cancel(true);
        }

        SparseArray<RegisteringService> container = dataHolder.getRegisteringServiceContainer();
        synchronized (container) {
            container.remove(exam.getId());
        }
    }

    private void runRegistering() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        Helper.appendLog("Preparing premature request");
        Response response = dataHolder.getNetworkInterface().execute(getRegisterRequest(), Response.Type.TEXT);
        if (examList.onRegistrationResponse(this, exam, response)) {
            notifyUser();
            Helper.appendLog("Register success!");
            stopSelf();
            return;
        }

        long serverTime = response.getServerTime().getTime();
        long serverTimeToReg = exam.getRegisterStart().getTime() - serverTime;
        Helper.appendLog("Actual registering will start in " + String.valueOf(serverTimeToReg / 1000L));
        if (serverTimeToReg > 0) {
            long delay = System.currentTimeMillis() - startTime;
            long timeToReg = serverTimeToReg - delay;
            if (timeToReg > 5000) {
                Thread.sleep(timeToReg - 5000);
            }
        } else
            Helper.appendLog("Request starting should start ASAP");

        Helper.appendLog("Actual request sending starting now");


        // now spam registering
        startTime = System.currentTimeMillis();
        while(!exam.isRegistered() &&
            System.currentTimeMillis() - startTime < 15000) { // try only for 15 seconds tops

            final TextNetworkTask task = new TextNetworkTask(this, getRegisterRequest());
            task.setResponseCallback(new BaseNetworkTask.ResponseCallback() {
                @Override
                public void onSuccess(Response response) {
                    tasks.remove(task);
                    if (examList.onRegistrationResponse(RegisteringService.this, exam, response)) {
                        notifyUser();
                        Helper.appendLog("Register success!");
                        stopSelf();
                    } else {
                        Helper.appendLog("Register unsuccessful");
                    }
                }
            });

            tasks.add(task);
            BaseNetworkTask.run(task);
            Helper.appendLog("Starting new task");
            Thread.sleep(REQUEST_TIME_DIFF_MS);
        }
    }

    private void notifyUser() {
        if (MainActivity.getInstance() != null) {
            Toast.makeText(getApplicationContext(), "Přihlášení bylo úspěšné", Toast.LENGTH_LONG).show();
        } else {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

            builder.setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle("Příhlášen na termín")
                    .setContentText("Byl jste přihlášen na termín předmětu " + exam.getCourseName() +
                        " dne " + Helper.getDateOutput(exam.getExamDate(), Helper.DateOutputType.DATE_TIME) + ".");

            Intent resultIntent = new Intent(this, MainActivity.class);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent( 0, PendingIntent.FLAG_UPDATE_CURRENT );
            builder.setContentIntent(resultPendingIntent);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(exam.getId(), builder.build());
        }
    }

    private HttpRequestBase getRegisterRequest() {
        return HttpRequestBuilder.getRegisterRequest(dataHolder, exam, true);
    }

    public static AlarmManager getAlarmManager(Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public static void setExamRegister(Context context, Exam exam) {
        NetworkInterface networkInterface = DataHolder.getInstance(context).getNetworkInterface();
        long targetInMillis = exam.getRegisterStart().getTime() - networkInterface.getLastServerLocalTimeDiff();
        targetInMillis -= 30 * 1000;
        long startInMillis = targetInMillis - System.currentTimeMillis();

        Helper.appendLog("Planing exam registering. Registering should start on `" +
            Helper.getDateOutput(targetInMillis, Helper.DateOutputType.DATE_TIME) +
            "`, which is in " + String.valueOf(startInMillis / 1000L) + " seconds");

        if (startInMillis < 0) {
            Toast.makeText(context, "Předmět bude registrován během několika vteřin", Toast.LENGTH_LONG).show();
            Helper.appendLog("Exam register time is too soon, registering should start right away.");
            sendPendingIntent(getAproxRegisterPI(context, exam, false));
        } else {
            Toast.makeText(context, "Exam will be registered in " + Helper.secondsCountdown(startInMillis, false), Toast.LENGTH_LONG).show();
            Helper.appendLog("Exam registering will start in " + Helper.secondsCountdown(startInMillis, false));
            getAlarmManager(context).set(AlarmManager.RTC_WAKEUP, targetInMillis, getAproxRegisterPI(context, exam, false));
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
