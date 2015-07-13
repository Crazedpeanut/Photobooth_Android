package net.johnkendall.cameraapp;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import java.io.File;
import java.util.Random;

public class SendFileService extends IntentService implements FileSendThreadInterface
{
    File files[];
    FileSender fileSender;

    int notificationId;
    NotificationManager notificationManager;

    public SendFileService()
    {
        super("SendFileService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        notificationId = new Random().nextInt();
        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        fileSender = new FileSender(this, this);

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String[] filePaths = intent.getStringArrayExtra("filePaths");

        files = new File[filePaths.length];

        for(int i = 0; i < filePaths.length; i++)
        {
            files[i] = new File(filePaths[i]);
        }

        fileSender.sendFiles(files);
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void handleFileSendThreadCompletionSuccess(String threadName) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setSmallIcon(R.drawable.John_Kendall_Logo_Black);
        notificationBuilder.setContentTitle(getResources().getString(R.string.app_name));
        notificationBuilder.setContentText(getResources().getString(R.string.files_sent_confirmation));
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    @Override
    public void handleFileSendThreadCompletionFailure(String threadName, Exception e) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setSmallIcon(R.drawable.John_Kendall_Logo_Black);
        notificationBuilder.setContentTitle(getResources().getString(R.string.app_name));
        notificationBuilder.setContentText(getResources().getString(R.string.files_failed_sent_confirmation));
        notificationBuilder.setColor(getResources().getColor(android.R.color.holo_red_dark));
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    @Override
    public void handleFileSendCompletionCount(String threadName, int currentByteCount, int totalByteCount)
    {

    }
}
