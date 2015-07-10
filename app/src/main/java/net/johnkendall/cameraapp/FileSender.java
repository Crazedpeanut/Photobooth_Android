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
    final static int INTEGER_SIZE = 4;

    Context context;

    public FileSender(Context context)
    {
        this.context = context;
    }

    static byte[] imagesToBuffer(File[] files)
    {
        byte[][] fileNames;
        byte[][] fileBytes;
        int[] fileSizes;
        ByteBuffer buffer;
        int[] fileNameSizes;
        int numFiles;
        int bufferSize = 0;

        FileInputStream fileInputStream;
        BufferedInputStream bufferedInputStream;

        numFiles = files.length;
        fileNames = new byte[files.length][];
        fileBytes = new byte[files.length][];
        fileSizes = new int[files.length];
        fileNameSizes = new int[files.length];

        bufferSize += INTEGER_SIZE; //For the total bytes required at the beginning of message
        bufferSize += INTEGER_SIZE; //For the number of files to send

        Log.d(TAG, "Preparing to buffer files");

        try
        {
            for (int i = 0; i < files.length; i++) {
                byte[] fileDataBuffer = new byte[(int) files[i].length()];

                fileNames[i] = files[i].getName().getBytes();
                bufferSize += fileNames[i].length;

                fileNameSizes[i] = fileNames[i].length;
                bufferSize += INTEGER_SIZE;

                fileSizes[i] = (int)files[i].length();
                bufferSize += INTEGER_SIZE;

                fileInputStream = new FileInputStream(files[i]);
                bufferedInputStream = new BufferedInputStream(fileInputStream);

                Log.d("FileSender", String.format("adding %s to Serilizable", files[i].getPath()));

                if (files[i].exists()) {
                    bufferedInputStream.read(fileDataBuffer, 0, (int) files[i].length());
                    fileBytes[i] = fileDataBuffer;

                    bufferSize += fileBytes[i].length;
                }
            }

            //Loading up the buffer
            buffer = ByteBuffer.allocate(bufferSize);

            //Put the total size of the message at the beginning, minus the space taken for the size of the message
            buffer.putInt(bufferSize - INTEGER_SIZE);

            buffer.putInt(numFiles); //Put the number of files to send

            for(int i : fileSizes) //Put each of the file sizes onto the buffer
            {
                buffer.putInt(i);
            }

            for(int i : fileNameSizes) //Put each of the file name sizes onto the buffer
            {
                buffer.putInt(i);
            }

            for(byte[] b : fileNames) //Put each of the filenames onto the buffer
            {
                buffer.put(b);
            }

            for(byte[] b : fileBytes)//Put each of the file byte arrays onto the buffer
            {
                buffer.put(b);
            }

            return buffer.array();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        return null;
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
