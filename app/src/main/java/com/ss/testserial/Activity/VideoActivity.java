package com.ss.testserial.Activity;

import android.app.Activity;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.VideoView;

import com.ss.testserial.Common.Common;
import com.ss.testserial.Common.Constants;
import com.ss.testserial.R;

import java.io.File;
import java.util.List;

public class VideoActivity extends Activity {
    private VideoView my_video;
    private List<File> file;
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        //隐藏状态栏
        Common.hideBottomUIMenu();
        index = 0;
        my_video = (VideoView) findViewById(R.id.my_video);
        file = Common.getFile(new File(Constants.path));
        setVideo();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Common.save("点击退出广告");
        finish();
        return true;
    }

    private void setVideo() {
        
        my_video.setVideoPath(file.get(index).getAbsolutePath());
        my_video.start();
        my_video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                    @Override
                    public boolean onInfo(MediaPlayer mp, int what, int extra) {
                        return true;
                    }
                });
            }
        });
        my_video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (index >= file.size() - 1) {
                    index = 0;
                } else {
                    index++;
                }
                my_video.setVideoPath(file.get(index).getAbsolutePath());
                my_video.start();
            }
        });

    }


}
