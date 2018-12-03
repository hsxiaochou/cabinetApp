package com.ss.testserial.Activity;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ss.testserial.Common.Common;
import com.ss.testserial.Common.Constants;
import com.ss.testserial.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

public class VideoActivity2 extends Activity implements
        SurfaceHolder.Callback {

    MediaPlayer player;
    private List<File> file;
    private MediaPlayer firstPlayer, //负责播放进入视频播放界面后的第一段视频
            nextMediaPlayer, //负责一段视频播放结束后，播放下一段视频
            cachePlayer, //负责setNextMediaPlayer的player缓存对象
            currentPlayer; //负责当前播放视频段落的player对象
    SurfaceView surface;
    SurfaceHolder surfaceHolder;


    //存放所有视频端的url
    private ArrayList<String> VideoListQueue = new ArrayList<String>();
    //所有player对象的缓存
    private HashMap<String, MediaPlayer> playersCache = new HashMap<String, MediaPlayer>();
    //当前播放到的视频段落数
    private int currentVideoIndex = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video2);
        file = Common.getFile(new File(Constants.path));
        initView();
    }

    private void initView() {
        surface = (SurfaceView) findViewById(R.id.surface);
        surfaceHolder = surface.getHolder(); // SurfaceHolder是SurfaceView的控制接口
        surfaceHolder.addCallback(this); // 因为这个类实现了SurfaceHolder.Callback接口，所以回调参数直接this
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
//surfaceView创建完毕后，首先获取该直播间所有视频分段的url
        getVideoUrls();
        //然后初始化播放手段视频的player对象
        initFirstPlayer();
    }

    private void initFirstPlayer() {
        firstPlayer = new MediaPlayer();
        firstPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        firstPlayer.setDisplay(surfaceHolder);

        firstPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                onVideoPlayCompleted(mp);
            }
        });
        //设置cachePlayer为该player对象
        cachePlayer = firstPlayer;
        initNexttPlayer();

        //player对象初始化完成后，开启播放
        startPlayFirstVideo();
    }

    private void startPlayFirstVideo() {
        try {
            firstPlayer.setDataSource(VideoListQueue.get(currentVideoIndex));
            firstPlayer.prepare();
            firstPlayer.start();
        } catch (IOException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }
    }

    private void initNexttPlayer() {
        new Thread(new Runnable() {

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void run() {

                for (int i = 0; i < VideoListQueue.size(); i++) {
                    nextMediaPlayer = new MediaPlayer();
                    nextMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    nextMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            onVideoPlayCompleted(mp);
                        }
                    });

                    try {
                        nextMediaPlayer.setDataSource(VideoListQueue.get(i));
                        nextMediaPlayer.prepare();
                    } catch (IOException e) {
                        // TODO 自动生成的 catch 块
                        e.printStackTrace();
                    }

                    //set next mediaplayer
                    cachePlayer.setNextMediaPlayer(nextMediaPlayer);
                    //set new cachePlayer
                    cachePlayer = nextMediaPlayer;

                    playersCache.put(String.valueOf(i), nextMediaPlayer);
                }

            }
        }).start();
    }


    private void onVideoPlayCompleted(MediaPlayer mp) {
        mp.setDisplay(null);
        //get next player
        if (currentVideoIndex == 1) {
            currentVideoIndex = 0;
        }else {
            currentVideoIndex++;
        }
        currentPlayer = playersCache.get(String.valueOf(currentVideoIndex));
        Log.e("TAG", "currenpayer" + playersCache.size());
        currentPlayer.setDisplay(surfaceHolder);
    }

    private void getVideoUrls() {
        for (int i = 0; i < 2; i++) {
            String url = getURI(i);
            VideoListQueue.add(url);
        }
    }

    private String getURI(int index) {
        String url = null;
        if (index == 0) {
            url = file.get(0).getAbsolutePath();
        } else if (index == 1) {
            url = file.get(1).getAbsolutePath();
        }
        return url;
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
// TODO Auto-generated method stub

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (firstPlayer != null) {
            if (firstPlayer.isPlaying()) {
                firstPlayer.stop();
            }
            firstPlayer.release();
        }
        if (nextMediaPlayer != null) {
            if (nextMediaPlayer.isPlaying()) {
                nextMediaPlayer.stop();
            }
            nextMediaPlayer.release();
        }

        if (currentPlayer != null) {
            if (currentPlayer.isPlaying()) {
                currentPlayer.stop();
            }
            currentPlayer.release();
        }
        currentPlayer = null;
    }// Activity销毁时停止播放，释放资源。不做这个操作，即使退出还是能听到视频播放的声音
}



