package com.ss.testserial.Runnable;

import android.net.NetworkInfo;
import android.util.Log;

import com.ss.testserial.Common.Common;
import com.ss.testserial.Common.Constants;
import com.ss.testserial.JNI.GridStatusListener;
import com.ss.testserial.JNI.Jubu;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Listen on 2016/9/9.
 */
public class BoardInfo implements Runnable {

    //锁控板信息
    public static int[] lockBoardInfo;
    //锁孔板实际个数
    public static int lockBoardCount = 0;
    //所有锁状态数组
    public static int[] lockStatus = new int[Constants.PER_MAX_LOCK_COUNT];
    //所有柜子是否有物品
    public static int[] infraredStatus = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    //发送注册设备JSON
    private JSONObject registerJson = new JSONObject();
    //发送设备状态JSON
    public static JSONArray lockStateJson = new JSONArray();
    //存储柜子最近的状态
    public static JSONObject boxLastStatus = null;

    @Override
    public void run() {
        //注册设备
        this.registerDevice();
    }

    /**
     * 获取初始化信息注册设备 info = {class:'',method:'',timestamp:'',sign:'',data:{}};
     */
    private void registerDevice() {
        Log.e("TAG", "开始注册");
        try {
            Common.log.write("开启border info线程");
            if (!Common.registerBoardThreadRun) {
                Log.d("板信息", "挂起注册");
                Thread.sleep(Constants.REGISTER_DEVICE_FAILED_DELAY);
                this.registerDevice();
            }
            while (true) {
                Thread.sleep(Constants.RECONNECT_DELAY);
                if (Common.socket.isConnected() && !Common.socket.isClosed()) {
                    break;
                }
                Common.log.write("注册板信息，尚未联网");
            }
            //获取锁控板信息,返回锁控板个数
            this.lockBoardCount = Common.lockBoard.size();
            this.lockBoardInfo = new int[this.lockBoardCount];
            for (int j = 0; j < this.lockBoardCount; j++) {
                this.lockBoardInfo[j] = Common.lockBoard.get(j);
            }
            JSONArray lockBoardJson = new JSONArray();
            for (int i = 0; i < lockBoardCount; i++) {
                lockBoardJson.put(this.lockBoardInfo[i]);
            }
            //获取主板mac地址
            while (Common.mac == null) {
                Thread.sleep(100);
            }
            JSONObject data = new JSONObject();
            //获取mac地址
            data.put("mac", Common.mac);
            //获取软件版本
            data.put("sv", Common.version);
            //获取硬件版本
            data.put("hv", android.os.Build.MODEL);
            //注册锁控板信息
            data.put("lock_board", lockBoardJson);
            //打包成json
            this.registerJson = Common.packageJsonData(Constants.REGISTER_DEVICE_JSON_CLASS, Constants.REGISTER_DEVICE_JSON_METHOD, data);
            //发送注册信息
            Common.put.println(Common.encryptByDES(this.registerJson.toString(), Constants.DES_KEY));
            Common.put.flush();
            Common.log.write("注册设备：" + this.registerJson.toString());
            //初始化柜子默认状态
            this.initBoxStatus();
            Common.isBoardRegister = true;
            //获取锁信息
            this.getLockInfo();
        } catch (Exception e) {
            Common.log.write("注册设备失败：" + e.toString());
            e.printStackTrace();
            try {
                Thread.sleep(Constants.REGISTER_DEVICE_FAILED_DELAY);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            this.registerDevice();
        }
    }

    /**
     * 获取锁信息
     */
    private void getLockInfo() {
        try {
            //循环获取锁状态
            while (true) {
                if (!Common.registerBoardThreadRun) {
                    Log.d("板信息", "挂起获取锁状态");
                    Thread.sleep(Constants.REGISTER_DEVICE_FAILED_DELAY);
                    continue;
                }
                if (!Common.isBoardRegister) {
                    Common.log.write("获取状态时设备未注册，重新注册");
                    Thread.sleep(Constants.REGISTER_DEVICE_FAILED_DELAY);
                    this.registerDevice();
                }

                Common.confirm_LockBoardVsersion();//2次判断LockBoardVsersion
                //板子数组，成员为对象boardJsonObject
                for (int i = 0; i < this.lockBoardCount; i++) {
                    // TODO:
                    if (Common.LockBoardVsersion.equals(Constants.THIRD_BOX_NAME)) {
                        Jubu.getAllStatus(this.lockBoardInfo[i]);
                    } else {
                        Common.device.getAllDoorStatus(this.lockBoardInfo[i], new GridStatusListener() {
                            @Override
                            public void getStatusEnd(int boardID, HashMap<String, Boolean> DoorStatus) {

                            }
                        });
                    }
                }
                Thread.sleep(Constants.GET_LOCK_STATE_DELAY);
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                Thread.sleep(Constants.REGISTER_DEVICE_FAILED_DELAY);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            this.registerDevice();
        }
    }

    /**
     * 初始化柜子默认状态
     */
    private void initBoxStatus() {
        try {
            //板子数组，成员为对象boardJsonObject
            this.boxLastStatus = new JSONObject();
            //遍历所有锁控板
            for (int i = 0; i < this.lockBoardCount; i++) {
                //板子对象board_id,locks
                JSONObject boardJsonObject = new JSONObject();
                //遍历每个板子的锁数组，成员为对象lockJsonObject
                JSONArray lockJsonArray = new JSONArray();
                for (int j = 0; j < Constants.PER_MAX_LOCK_COUNT; j++) {
                    //柜子对象lock_id,open_status,infrared_status
                    JSONObject lockJsonObject = new JSONObject();
                    lockJsonObject.put("lock_id", j + 1);
                    lockJsonObject.put("open_status", 0);
                    lockJsonObject.put("infrared_status", 0);
                    lockJsonArray.put(lockJsonObject);
                }
                boardJsonObject.put("lock_board", this.lockBoardInfo[i]);
                boardJsonObject.put("locks", lockJsonArray);
                this.boxLastStatus.put("board" + this.lockBoardInfo[i], boardJsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
