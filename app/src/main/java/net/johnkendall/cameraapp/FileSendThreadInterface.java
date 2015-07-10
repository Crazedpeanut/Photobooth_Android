package net.johnkendall.cameraapp;

/**
 * Created by John on 9/07/2015.
 */
public interface FileSendThreadInterface {
    void handleFileSendThreadCompletionFailure(String threadName, Exception e);
    void handleFileSendThreadCompletionSuccess(String threadName);
}
