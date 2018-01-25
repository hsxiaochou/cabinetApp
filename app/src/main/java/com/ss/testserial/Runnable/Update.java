package com.ss.testserial.Runnable;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.ss.testserial.Common.Common;
import com.ss.testserial.Common.Constants;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Listen on 2016/11/17.
 */
public class Update {

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case Constants.APK_UPDATE_SUCCESS_MESSAGE:
                    install(msg.obj.toString());
                    break;
                default:
                    break;
            }
        }
    };
    private Context context;

    public Update(Context context){
        this.context = context;
    }

    public void update(final String url){
        Common.startLoad();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection conn =  (HttpURLConnection) new URL(url).openConnection();
                    conn.setConnectTimeout(30000);
                    InputStream is = conn.getInputStream();
                    File file = new File(Environment.getExternalStorageDirectory(), "yougoto.apk");
                    FileOutputStream fos = new FileOutputStream(file);
                    BufferedInputStream bis = new BufferedInputStream(is);
                    byte[] buffer = new byte[1024];
                    int len ;
                    int total=0;
                    while((len =bis.read(buffer))!=-1){
                        fos.write(buffer, 0, len);
                        total+= len;
                    }
                    fos.close();
                    bis.close();
                    is.close();
                    Message message = new Message();
                    message.what = Constants.APK_UPDATE_SUCCESS_MESSAGE;
                    message.obj = file.getAbsolutePath();
                    handler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                    Common.endLoad();
                }

            }
        }).start();
    }

    /**
     * 升级
     */
    private void install(String filePath){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(filePath));
        intent.setDataAndType(uri,"application/vnd.android.package-archive");
        this.context.startActivity(intent);
    }
}
