package net.johnkendall.cameraapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.prefs.Preferences;

/**
 * Created by John on 30/05/2015.
 */
public class FileSender implements FileSendThreadInterface
{
    final static String TAG = "FileSender";

    Context context;

    public FileSender(Context context)
    {
        this.context = context;
    }

    static FilesSerializable serializeFiles(File[] files)
    {
        String[] fileNames;
        byte[][] fileBytes;

        FileInputStream fileInputStream;
        BufferedInputStream bufferedInputStream;

        fileNames = new String[files.length];
        fileBytes = new byte[files.length][];

        Log.d("FileSender", "Preparing to serialize files");

        try
        {
            for(int i = 0; i < files.length; i++)
            {
                byte[] fileDataBuffer = new byte[(int)files[i].length()];
                fileNames[i] = files[i].getName();


                fileInputStream = new FileInputStream(files[i]);
                bufferedInputStream = new BufferedInputStream(fileInputStream);

                Log.d(TAG, String.format("adding %s to Serilizable",files[i].getPath()));

                if(files[i].exists()) {
                    bufferedInputStream.read(fileDataBuffer, 0, (int) files[i].length());

                    fileBytes[i] = fileDataBuffer;
                }


                else {
                    Log.d(TAG, String.format("%s does not exist!",files[i].getPath()));
                }

                fileInputStream.close();
                bufferedInputStream.close();
            }

            Log.d(TAG, String.format("Done serializing files!"));
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        return new FilesSerializable(fileNames, fileBytes);
    }

    public void sendFiles(File[] files)
    {
        for(File f : files)
        {
            Log.d(TAG, String.format("Preparing to send: %s",f.getPath()));
        }

        Thread thread = new Thread(new FileSendThread(files, context, this));
        thread.start();
    }

    @Override
    public void handleFileSendThreadCompletionFailure(String threadName, Exception e) {
        Log.d(TAG, String.format("%s failed with Exception: %s", threadName, e.getMessage()));
    }

    @Override
    public void handleFileSendThreadCompletionSuccess(String threadName) {
        Log.d(TAG, String.format("%s completed successfully", threadName));
    }
}
