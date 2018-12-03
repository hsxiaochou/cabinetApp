package com;

import android.app.Application;
import android.content.Context;
import android.net.wifi.WifiManager;

import com.ss.testserial.Common.Common;
import com.ss.testserial.Common.FileLog;
import com.ss.testserial.Common.TimerTask.BannerTimerTask;
import com.ss.testserial.Common.TimerTask.GetBoxTimerTask;
import com.ss.testserial.Common.TimerTask.RebotTimerTask;
import com.ss.testserial.Common.TimerTask.RestartTimerTask;
import com.ss.testserial.Runnable.Wifi;
import com.tencent.bugly.crashreport.CrashReport;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2018/2/6 0006.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        //腾讯buly检测
        CrashReport.initCrashReport(getApplicationContext(), "2b8c391d65", false);


        Common.applicationContext = getApplicationContext();
        //开启定时启动功能
        doRebot();
        Common.context = getApplicationContext();
        // 修改
        //开启获取WIFI线程
        if (Common.mac == null) {
            new Thread(new Wifi((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE))).start();
        }
        //开启获取柜子格数
        GetBoxNum();
        //k开启倒计时
        Dorestart();
        //开启banner
        DoBanner();
        Common.log = new FileLog();
        Common.log.write("打开优裹徒");


    }

    private void DoBanner() {
        new Timer().schedule(new BannerTimerTask(), 10000, 10000);
    }

    private void Dorestart() {
        new Timer().schedule(new RestartTimerTask(), 1000, 1000);
    }


    private void GetBoxNum() {
        new Timer().schedule(new GetBoxTimerTask(), 60000, 600000);
    }

    private void doRebot() {
        TimerTask task = new RebotTimerTask();
        long delytiem = 1000 * 60 * 80;
        //间隔：1小时
        long period = 1000 * 60 * 60;
        Timer timer = new Timer();
        timer.schedule(task, delytiem, period);
    }}