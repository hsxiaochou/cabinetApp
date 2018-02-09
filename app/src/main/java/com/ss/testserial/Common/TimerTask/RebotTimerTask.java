package com.ss.testserial.Common.TimerTask;

import android.util.Log;

import com.ss.testserial.Common.Common;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimerTask;

/**
 * Created by Administrator on 2018/2/6 0006.
 */

public class RebotTimerTask extends TimerTask {
    //重启时间
    private static List<Integer> eatTimes;

    /*
     * 静态初始化
     * */
    static {
        initEatTimes();
    }
    /*
     * 初始化吃饭时间
     * */
    private static void initEatTimes() {
        eatTimes = new ArrayList<Integer>();
        eatTimes.add(6);
    }

    @Override
    public void run() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        Log.e("TAG", hour + "  ");
        if (eatTimes.contains(hour)) {
           Common.rebot();
        }
    }
}
