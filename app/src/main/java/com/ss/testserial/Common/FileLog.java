package com.ss.testserial.Common;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Listen on 2017/1/25.
 */
public class FileLog {

    private File file;
    private FileOutputStream out;
    private SimpleDateFormat dateFormat;

    public FileLog() {
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        /*
        this.file = new File(Environment.getExternalStorageDirectory(),Constants.UNCAUGHT_EXCEPTION_LOG_FIEL+new SimpleDateFormat("yyyy_MM_dd").format(new Date())+".log");

        try {
            this.out = new FileOutputStream(this.file,true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        */
    }

    public void write(String log) {
        Common.save(log);
        //Log.d("yougoto",("["+this.dateFormat.format(new Date())+"]:"+log+"\n"));
/*
        try {

            this.out.write( ("["+this.dateFormat.format(new Date())+"]:"+log+"\n").getBytes("UTF-8") );
        } catch (IOException e) {
            e.printStackTrace();
        }
        */

    }

    public void close() {
        try {
            this.out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
