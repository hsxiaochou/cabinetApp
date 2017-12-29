package com.ss.testserial.JNI;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.ss.testserial.Common.Common;
import com.ss.testserial.Common.Constants;

/**
 * Created by leon on 2017/9/11.
 */
//TODO:
public class Jubu {
    public static final String OPEN_DOOR = "com.hzjubu.action.REQ_OPEN_DOOR";
    public static final String GET_ALL_STATUS = "com.hzjubu.action.REQ_DOORS_STATUS";

    public static void openBox(int boardId, int lockId) {
        Intent intent = new Intent(Jubu.OPEN_DOOR);
        intent.putExtra("iBoardId", boardId);
        intent.putExtra("iLockId", lockId);
        Common.mainActivity.sendBroadcast(intent);
    }

    public static void getAllStatus(int boardId) {
        Intent intent = new Intent(Jubu.GET_ALL_STATUS);
        intent.putExtra("iBoardId", boardId);
        intent.putExtra("iBoxesCounts", Constants.PER_MAX_LOCK_COUNT);
        Common.mainActivity.sendBroadcast(intent);
    }
}
