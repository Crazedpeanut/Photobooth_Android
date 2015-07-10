package net.johnkendall.cameraapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by John on 30/05/2015.
 */
public class CaptureImages implements Runnable
{
    final String TAG = "CaptureImages";

    SharedPreferences prefs;

    Camera camera;
    Camera.PictureCallback jpegCallBack;
    Camera.ShutterCallback shutterCallback;
    long imageCaptureRate;

    int timeBetweenSnaps = 2000;


    public CaptureImages( Camera camera, Camera.PictureCallback jpegCallBack,Camera.ShutterCallback shutterCallback, Context context)
    {
        this.camera = camera;
        this.jpegCallBack = jpegCallBack;
        this.shutterCallback = shutterCallback;
    }

    @Override
    public void run()
    {
        camera.takePicture(shutterCallback, null, jpegCallBack);
    }
}
