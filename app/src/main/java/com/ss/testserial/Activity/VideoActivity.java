package com.ss.testserial.Activity;

import android.app.Activity;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
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
        index = 0;
        my_video = (VideoView) findViewById(R.id.my_video);
        file = Common.getFile(new File(Constants.path));
        setVideo();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
                finish();
                break;
            case MotionEvent.ACTION_MOVE:
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
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
                        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START)
                            my_video.setBackgroundColor(Color.TRANSPARENT);
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
                Log.e("TAG", index + " ");
                my_video.setVideoPath(file.get(index).getAbsolutePath());
                my_video.start();
            }
        });
    }
}
