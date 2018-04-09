package com.ss.testserial.JNI;

import android.os.Message;
import android.util.Log;

import com.ss.testserial.Common.Common;
import com.ss.testserial.Common.Constants;
import com.ss.testserial.Runnable.BoardInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Listen on 2016/9/9.
 * 二代柜接口
 */
public class DeviceInterface implements RS485Comm.RS485CommInterface {
    private OpenGridListener openGridListener;
    private GridStatusListener gridStatusListener;
    private RS485Comm device;
    public Scanner scanner;

    public DeviceInterface() {
        try {
            this.scanner = new Scanner();
            this.device = RS485Comm.getinstance("/dev/ttyS3", 9600, 8, 0, 1);
            this.device.setCommInterface(DeviceInterface.this);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取所有板地址
     */
    public void getAllDoorStatus(int boardId, GridStatusListener gridStatusListener) {
        try {
            this.gridStatusListener = gridStatusListener;
            this.device.updateDoorStatusCmd(boardId);
        } catch (NullPointerException e) {
            Message message = Common.mainActivityHandler.obtainMessage();
            message.obj = "没有主板";
            message.what = Constants.COMMON_ERROR_MESSAGE;
            message.sendToTarget();
        }
    }

    /**
     * 开柜
     *
     * @param boardId
     * @param doorId
     */
    public int openGrid(int boardId, int doorId, OpenGridListener openGridListener) {
        this.openGridListener = openGridListener;
        Common.log.write("打开柜子：[border：" + boardId + "][door：" + doorId + "]");
        return this.device.openGridCmd(boardId, doorId);
    }

    @Override
    public void onOpenGridEnd(int commandID, int boardID, int doorID) {
        this.openGridListener.openEnd();
    }

    @Override
    public void onUpdateDoorStatusEnd(int commandID, int boardID, HashMap<String, Boolean> DoorStatus) {

        //获取状态
        if (commandID < 0 || DoorStatus == null) {
            BoardInfo.lockStateJson = new JSONArray();
            return;
        }
        for (int i = 0; i < DoorStatus.size() && i < BoardInfo.lockStatus.length; i++) {
            if (DoorStatus.get("door" + (i + 1))) {
                BoardInfo.lockStatus[i] = 1;
            } else {
                BoardInfo.lockStatus[i] = 2;
            }
        }
        try {
            JSONArray locks = BoardInfo.boxLastStatus.getJSONObject("board" + boardID).getJSONArray("locks");
            JSONArray lockJsonArray = new JSONArray();
            for (int i = 0; i < locks.length(); i++) {
                if (locks.getJSONObject(i).getInt("open_status") != BoardInfo.lockStatus[i] || locks.getJSONObject(i).getInt("infrared_status") != BoardInfo.infraredStatus[i]) {
                    //柜子对象lock_id,open_status,infrared_status
                    JSONObject lockJsonObject = new JSONObject();
                    lockJsonObject.put("lock_id", i + 1);
                    lockJsonObject.put("open_status", BoardInfo.lockStatus[i]);
                    lockJsonObject.put("infrared_status", BoardInfo.infraredStatus[i]);
                    lockJsonArray.put(lockJsonObject);
                    //修改最近获取的数据
                    locks.getJSONObject(i).put("open_status", BoardInfo.lockStatus[i]);
                    locks.getJSONObject(i).put("infrared_status", BoardInfo.infraredStatus[i]);
                }
            }
            JSONObject board = new JSONObject();
            board.put("lock_board", boardID);
            board.put("locks", lockJsonArray);
            Common.log.write("获取锁状态：" + board.toString());
            BoardInfo.lockStateJson.put(board);
            //如果获取完毕提交

            if (BoardInfo.lockStateJson.length() == BoardInfo.lockBoardCount) {

                if (Common.isLockStatusChange(BoardInfo.lockStateJson)) {
                    Common.log.write("上报锁状态：" + BoardInfo.lockStateJson.toString());
                    Common.put.println(Common.encryptByDES(Common.packageJsonData(Constants.LOCK_STATUS_JSON_CLASS, Constants.LOCK_STATUS_JSON_METHOD, BoardInfo.lockStateJson).toString(), Constants.DES_KEY));
                    Common.put.flush();
                } else {
                    Common.log.write("上报锁状态：状态未改变");
                }
                //重置
                BoardInfo.lockStateJson = new JSONArray();
            }
        } catch (Exception e) {
            e.printStackTrace();
            //重置
            BoardInfo.lockStateJson = new JSONArray();
        }
    }

    @Override
    public void onUpdateIrStatusEnd(int commandID, int boardID, HashMap<String, Boolean> DoorStatus) {

    }
}
