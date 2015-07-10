package net.johnkendall.cameraapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by John on 9/07/2015.
 */
public class FileSendThread implements Runnable
{
    Socket socket;

    File[] files;
    SharedPreferences sharedPreferences;
    FileSendThreadInterface fileSendThreadInterface;

    String host;
    int port;

    public FileSendThread(File[] files, Context context, FileSendThreadInterface fileSendThreadInterface)
    {
        this.files = files;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        host = sharedPreferences.getString("server_hostname", "localhost");
        port = Integer.valueOf(sharedPreferences.getString("server_port", "9999"));

        this.fileSendThreadInterface = fileSendThreadInterface;
    }

    public FileSendThread(File[] files, Context context, FileSendThreadInterface fileSendThreadInterface, String host, int port)
    {
        this.files = files;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.host = host;
        this.port = port;
        this.fileSendThreadInterface = fileSendThreadInterface;
    }


    public void run()
    {
        FilesSerializable filesSerializable;
        socket = new Socket();
        String threadName;
        OutputStream outputStream;
        OutputStreamWriter outputStreamWriter;
        BufferedOutputStream bufferedOutputStream;
        byte[] buffer;

        filesSerializable = new FilesSerializable(files);

        buffer = filesSerializable.toByteArray();
        threadName = Thread.currentThread().getName();

        try
        {
            socket.connect(new InetSocketAddress(host, port));

            outputStream = socket.getOutputStream();
            bufferedOutputStream = new BufferedOutputStream(outputStream);

            bufferedOutputStream.write(buffer);

            outputStream.close();
            bufferedOutputStream.close();

            socket.close();

            fileSendThreadInterface.handleFileSendThreadCompletionSuccess(threadName);
        }
        catch(IOException e)
        {
            fileSendThreadInterface.handleFileSendThreadCompletionFailure(threadName, e);
        }


    }
}
