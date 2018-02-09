package com.ss.testserial.Common.TimerTask;

import android.util.Log;

import com.ss.testserial.Common.Common;

import java.util.TimerTask;

/**
 * Created by Administrator on 2018/2/6 0006.
 */

public class GetBoxTimerTask extends TimerTask {
    @Override
    public void run() {
        if (Common.frame == "main") {
            Common.getCabinetLeft();
        }
    }
}
