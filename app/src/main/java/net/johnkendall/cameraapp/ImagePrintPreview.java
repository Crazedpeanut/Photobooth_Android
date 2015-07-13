package net.johnkendall.cameraapp;

import android.content.Context;
import android.content.Intent;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;


public class ImagePrintPreview extends ActionBarActivity {

    final String TAG = "ImagePrintPreview";

    WebView previewWebView;
    File[] images;
    Intent intent;
    FileSender fileSender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_print_preview);

        intent = getIntent();
        images = new File[4];

        images[0] = new File(getFilesDir().getPath() + "/" + intent.getStringExtra("image0"));
        images[1] = new File(getFilesDir().getPath() + "/" + intent.getStringExtra("image1"));
        images[2] = new File(getFilesDir().getPath() + "/" + intent.getStringExtra("image2"));
        images[3] = new File(getFilesDir().getPath() + "/" + intent.getStringExtra("image3"));

        previewWebView = (WebView)findViewById(R.id.previewWebView);
        previewWebView.setWebViewClient(new WebViewClient(){

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.i(TAG, "page finished loading " + url);
                //createWebPrintJob(view);

                for(File f : images)
                {
                    Log.d(TAG, String.format("File Name: %s",f.getName()));
                }
            }
        });

        // Generate an HTML document on the fly:
        String htmlDocument = getHtmlDoc();
        try
        {
            previewWebView.loadDataWithBaseURL(getFilesDir().toURL().toString(), htmlDocument, "text/HTML", "UTF-8", null);
        }
        catch(MalformedURLException e)
        {
            e.printStackTrace();
        }

    }

    public String getHtmlDoc()
    {
        BufferedReader bufferedReader = null;
        String htmlDoc = "" ;

        try{
            bufferedReader = new BufferedReader(new InputStreamReader(getAssets().open("imagepreview4x6.html"), "utf-8"));

            String line = bufferedReader.readLine();
            while(line != null)
            {
                htmlDoc += line;

                line = bufferedReader.readLine();
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        finally {
            try
            {
                if(bufferedReader != null)
                {
                    bufferedReader.close();
                }
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }

        return htmlDoc;
    }

    public void createWebPrintJob(WebView v)
    {
        PrintManager printManager = (PrintManager)getSystemService(Context.PRINT_SERVICE);

        PrintDocumentAdapter printDocumentAdapter = v.createPrintDocumentAdapter();

        String jobName = getString(R.string.app_name);

        PrintJob printJob = printManager.print(jobName, printDocumentAdapter, new PrintAttributes.Builder().build());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_image_preview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
