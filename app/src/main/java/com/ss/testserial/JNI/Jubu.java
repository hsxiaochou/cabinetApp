package com.ss.testserial.JNI;

import android.app.Activity;
import android.content.Intent;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.ss.testserial.Common.Common;
import com.ss.testserial.Common.Constants;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by leon on 2017/9/11.
 */
//TODO:
public class Jubu {
    public static final String OPEN_DOOR = "com.hzjubu.action.REQ_OPEN_DOOR";
    public static final String GET_ALL_STATUS = "com.hzjubu.action.REQ_DOORS_STATUS";
    public static final String GET_DOOR_STATUS = "com.hzjubu.action.REQ_DOOR_STATUS";

    public static void openBox(int boardId, int lockId) {
        Common.savePreference("check_boardId", boardId + "");
        Common.savePreference("check_lockId", lockId + "");
        Intent intent = new Intent(Jubu.OPEN_DOOR);
        intent.putExtra("iBoardId", boardId);
        intent.putExtra("iLockId", lockId);

        //再次开柜
        JSONObject grid_info = new JSONObject();
        try {
            grid_info.put("boardId", boardId);
            grid_info.put("lockId", lockId);
            Common.open_again_data=grid_info;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Common.mainActivity.sendBroadcast(intent);
//        Message msg = new Message();
//        msg.what = Constants.OPEN_DOOR;
//        Common.mainActivityHandler.sendMessageDelayed(msg, 20000);
    }

    public static void getAllStatus(int boardId) {
        Intent intent = new Intent(Jubu.GET_ALL_STATUS);
        intent.putExtra("iBoardId", boardId);
        intent.putExtra("iBoxesCounts", Constants.PER_MAX_LOCK_COUNT);
        Common.mainActivity.sendBroadcast(intent);
    }

//    public static void getDoorStatus(int boardId, int lockId) {
//        Common.save("doorstatus  " + boardId + "   " + lockId);
//        Intent localIntent = new Intent(GET_DOOR_STATUS);
//        localIntent.putExtra("iBoardId", boardId);
//        localIntent.putExtra("iLockId", lockId);
//        Common.mainActivity.sendBroadcast(localIntent);
//    }
}
