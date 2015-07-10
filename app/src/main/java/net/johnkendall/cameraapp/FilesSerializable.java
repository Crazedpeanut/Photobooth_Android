package net.johnkendall.cameraapp;

import java.io.Serializable;

/**
 * Created by John on 6/06/2015.
 */
public class FilesSerializable implements Serializable
{
    static final long serialVersionUID = 42L;

    public String[] fileNames;
    public byte[][] fileBytes;

    public FilesSerializable(String[] fileNames, byte[][] fileBytes)
    {
        this.fileNames = fileNames;
        this.fileBytes = fileBytes;
    }
}
