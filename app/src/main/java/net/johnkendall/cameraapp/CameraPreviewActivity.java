package net.johnkendall.cameraapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.io.IOException;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CameraPreviewActivity extends Activity implements SurfaceHolder.Callback
{
    final String TAG = "CameraPreviewClass";

    Camera mCamera;
    SurfaceHolder surfaceHolder;
    SurfaceView surfaceView;

    TextView infoDialog;

    Camera.PictureCallback rawCallback;
    Camera.ShutterCallback shutterCallback;
    Camera.PictureCallback jpegCallback;

    ImageView imageNumIndicator;

    SharedPreferences sharedPreferences;

    int imageIndex;
    int countDown;
    int timeBetweenSnaps;

    Timer timer;
    TimerTask timerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_capture);

        imageIndex = 0;
        countDown = 0;

        timer = new Timer();
        initTimerTask();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        timeBetweenSnaps = Integer.valueOf(sharedPreferences.getString(getResources().getString(R.string.pref_default_image_capture_rate), "3000"));

        surfaceView = (SurfaceView)findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();

        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        imageNumIndicator = (ImageView)findViewById(R.id.imageCaptureTimer);
        imageNumIndicator.setImageResource(android.R.color.transparent);

        infoDialog = (TextView)findViewById(R.id.imagePreviewDialog);

        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "Pressed");

                try
                {
                    captureImages(v);
                    //Toast.makeText(getApplicationContext(), "Taking pictures..", Toast.LENGTH_LONG).show();
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
            }
        });

        shutterCallback = new Camera.ShutterCallback()
        {
            @Override
            public void onShutter()
            {
                AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                audioManager.playSoundEffect(AudioManager.FLAG_PLAY_SOUND);
            }
        };

        jpegCallback = new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                FileOutputStream outputStream = null;
                Log.d(TAG, String.format("Saving image: %s/%d.jpg", getFilesDir().toString(), imageIndex));

                try
                {
                    outputStream = openFileOutput(String.format("%d.jpg", imageIndex), Context.MODE_PRIVATE);
                    outputStream.write(data);
                    outputStream.close();


                    //Sending file to server
                    /*
                    File file = new File(String.format("%d.jpg",imageIndex));
                    URL url = file.toURL();

                    Runnable fileSender = new FileSender(getApplicationContext(),url);
                    new Thread(fileSender).start();*/

                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
                finally {

                }

                if(imageIndex == 3)
                {
                    imageNumIndicator.setImageResource(android.R.color.transparent);

                    Intent intent = new Intent(getApplicationContext(), ImagePreview.class);
                    intent.putExtra("image0","0.jpg");
                    intent.putExtra("image0","1.jpg");
                    intent.putExtra("image0","2.jpg");
                    intent.putExtra("image0","3.jpg");
                    startActivity(intent);
                }

                imageIndex++;

                refreshCamera();

            }
        };

    }



    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        try {
            mCamera = Camera.open();
        }
        catch (RuntimeException e) {
            System.err.println(e);
            return;
        }

        refreshCamera();
    }


    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio=(double)h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3)
    {
        refreshCamera();
    }

    public void refreshCamera() {

        if (surfaceHolder.getSurface() == null)
        {
            return;
        }

        try
        {
            mCamera.stopPreview();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Display display = getWindowManager().getDefaultDisplay();

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;

        List<Camera.Size> mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();

        Camera.Size previewSize = getOptimalPreviewSize(mSupportedPreviewSizes,width, height );
        width = previewSize.width;
        height = previewSize.height;

        Camera.Parameters parameters = mCamera.getParameters();
        /*Display display = ((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay();*/

        Log.d(TAG, String.format("Surface Rotation: %d", display.getRotation()));

        if(display.getRotation() == Surface.ROTATION_0)
        {
            parameters.setPreviewSize(width, height);
            parameters.set("orientation", "portrait");
            mCamera.setDisplayOrientation(90);
            Log.d(TAG, String.format("0"));
        }

        if(display.getRotation() == Surface.ROTATION_90)
        {
            parameters.setPreviewSize(height, width);
            parameters.set("orientation", "landscape");
            mCamera.setDisplayOrientation(0);
            Log.d(TAG, String.format("90"));
        }

        if(display.getRotation() == Surface.ROTATION_180)
        {
            parameters.setPreviewSize(width, height);
            Log.d(TAG, String.format("180"));
        }

        if(display.getRotation() == Surface.ROTATION_270)
        {
            parameters.setPreviewSize(width, height);
            parameters.set("orientation", "landscape");
        }

        mCamera.setParameters(parameters);

        try {
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();
        }

        catch (Exception e) {
            System.err.println(e);
            return;
        }

        try
        {
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void captureImages(View v) throws IOException
    {
        String[] files = fileList();
        for(String f : files)
        {
            if(f.contains(".jpg"))
            {
                deleteFile(f);
            }
        }

        imageIndex = 0;
        countDown = 4;

        /*timer = new Timer();
        initTimerTask();*/
        timer.schedule(timerTask, 0, timeBetweenSnaps/4);

    }

    public void initTimerTask()
    {
        timerTask = new TimerTask() {
            @Override
            public void run() {


                if(countDown == 0)
                {
                    countDown = 4;

                    Log.d(TAG, String.format("Image index: %d", imageIndex));
                    Log.d(TAG, String.format("Countdown: %d", countDown));

                    if(imageIndex == 3)
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageNumIndicator.setImageResource(android.R.color.transparent);
                                infoDialog.setText(getResources().getString(R.string.image_prev_instructions));
                            }
                        });

                        Log.d(TAG, "Stop image capturing");
                        timer.cancel();

                    }

                    CaptureImages captureImages = new CaptureImages(mCamera, jpegCallback,shutterCallback, getApplicationContext());
                    captureImages.run();
                }
                else
                {
                    Log.d(TAG, String.format("Countdown: %d", countDown));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            infoDialog.setText(String.format("Taking image %d of %d", imageIndex+1, 4));

                            switch (countDown) {
                                case 4:
                                    imageNumIndicator.setImageResource(R.drawable.four);
                                    break;
                                case 3:
                                    imageNumIndicator.setImageResource(R.drawable.three);
                                    break;
                                case 2:
                                    imageNumIndicator.setImageResource(R.drawable.two);
                                    break;
                                case 1:
                                    imageNumIndicator.setImageResource(R.drawable.one);
                                    break;
                            }
                        }
                    });

                    countDown--;
                }
            }
        };
    }


    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    public void surfaceDestroyed(SurfaceHolder arg0)
    {
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }
}