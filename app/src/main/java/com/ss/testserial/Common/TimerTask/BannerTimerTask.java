package com.ss.testserial.Common.TimerTask;

import com.ss.testserial.Common.Common;
import com.ss.testserial.Common.Constants;

import java.util.TimerTask;

/**
 * Created by Administrator on 2018/2/6 0006.
 */

public class BannerTimerTask extends TimerTask {
    @Override
    public void run() {
        Common.mainActivityHandler.sendEmptyMessage(Constants.SWITCH_BANNER_MESSAGE);
    }
}
