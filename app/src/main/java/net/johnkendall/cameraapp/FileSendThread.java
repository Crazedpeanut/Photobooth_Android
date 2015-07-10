package net.johnkendall.cameraapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by John on 9/07/2015.
 */
public class FileSendThread implements Runnable
{
    Socket socket;

    FilesSerializable filesSerializable;
    SharedPreferences sharedPreferences;
    FileSendThreadInterface fileSendThreadInterface;

    String host;
    int port;

    public FileSendThread(FilesSerializable filesSerializable, Context context, FileSendThreadInterface fileSendThreadInterface)
    {
        this.filesSerializable = filesSerializable;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        host = sharedPreferences.getString(context.getResources().getString(R.string.pref_default_server_hostname), "localhost");
        port = Integer.valueOf(sharedPreferences.getString(context.getResources().getString(R.string.pref_default_server_hostname), "9999"));

        this.fileSendThreadInterface = fileSendThreadInterface;
    }

    public FileSendThread(FilesSerializable filesSerializable, Context context, FileSendThreadInterface fileSendThreadInterface, String host, int port)
    {
        this.filesSerializable = filesSerializable;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.host = host;
        this.port = port;
        this.fileSendThreadInterface = fileSendThreadInterface;
    }


    public void run()
    {
        socket = new Socket();

        try
        {
            socket.connect(new InetSocketAddress(host, port));
        }
        catch(IOException e)
        {
            fileSendThreadInterface.handleFileSendThreadCompletionFailure(e);
        }


    }
}
