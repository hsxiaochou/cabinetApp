package com.ss.testserial.Common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ss.testserial.Activity.MainActivity;
import com.ss.testserial.JNI.Jubu;
import com.ss.testserial.JNI.OpenGridListener;
import com.ss.testserial.R;
import com.ss.testserial.Runnable.BoardInfo;
import com.ss.testserial.Runnable.TcpListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.internal.Util;

// TODO: 下面有开机自启动广播，如果广告有就去掉
public class BootBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION = "android.intent.action.BOOT_COMPLETED";
    public static final String JUBU_SCAN = "com.hzjubu.action.UP_BARCODE";//上报扫描事件
    public static final String JUBU_ALL_STATUS = "com.hzjubu.action.ACK_DOORS_STATUS";//获取指定副机所有箱格箱门状态.广播应答
    public static final String JUBU_OPEN_DOOR = "com.hzjubu.action.ACK_OPEN_DOOR";
    public static final String JUBU_DOOR_STATUS = "com.hzjubu.action.ACK_DOOR_STATUS";
    private int iErrorCode;

    @Override
    public void onReceive(final Context context, Intent intent) {
        switch (intent.getAction()) {
            //jubu开门的回馈
            case JUBU_OPEN_DOOR:
                //获取应答箱格所在的副机地址
                int iBoardId = intent.getIntExtra("iBoardId", -1);
                //获取应答箱格的格口ID
                int iLockId = intent.getIntExtra("iLockId", -1);
                iErrorCode = intent.getIntExtra("iErrorCode", -1);

                if (iErrorCode < 0) {//命令执行失败，提示失败原因：线路连接、硬件故障等
                    String sErrordesc = intent.getStringExtra("sErrordesc");
                    Common.save("错误消息：" + sErrordesc);
                } else {//命令执行成功，根据业务逻辑可进一步判断箱门是否被打开
                    boolean bOpend = intent.getBooleanExtra("bOpend", false);
                    Common.save("是否打开：" + bOpend);
                    if (bOpend == false) {//箱门被卡住，提示用户开箱重试

                    }
                }
                break;

            case ACTION:
                final Intent mainActivityIntent = new Intent(context, MainActivity.class);  // 要启动的Activity
                mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mainActivityIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                context.startActivity(mainActivityIntent);
                break;
            case JUBU_SCAN:
                String code = intent.getStringExtra("sBarcode");
                if (!TextUtils.isEmpty(code)) {
                    switch (Common.frame) {
                        case "scaner":
                            ((TextView) Common.mainActivity.findViewById(R.id.express)).setText(code);
                            dealScan(code);
                            break;
                        case "put":
                            ((EditText) Common.mainActivity.findViewById(R.id.express)).setText(code);
                            break;
                        default:
                            break;
                    }
                }
                break;
            case JUBU_ALL_STATUS:
                //获取出错
                int iErrorCode = intent.getIntExtra("iErrorCode", -1);
                if (iErrorCode < 0) {
                    String sErrordesc = intent.getStringExtra("sErrordesc");
                    return;
                }
                // 状态数组
                Bundle b = intent.getExtras();
                ArrayList<Integer> status = b.getIntegerArrayList("iOpendArray");
                for (int i = 0; i < status.size() && i < BoardInfo.lockStatus.length; i++) {
                    switch (status.get(i)) {
                        case 0:
                            //关闭
                            BoardInfo.lockStatus[i] = 1;
                            break;
                        case 1:
                            BoardInfo.lockStatus[i] = 2;
                            break;
                        default:
                            BoardInfo.lockStatus[i] = 2;
                            break;
                    }
                }
                //获取应答箱格所在的副机地址
                int boardID = intent.getIntExtra("iBoardId", -1);
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
                break;
            //JUBU获取指定门的状态
            case JUBU_DOOR_STATUS:
                int check_BoardId = intent.getIntExtra("iBoardId", -1);
                //获取应答箱格的格口ID
                int check_iLockId = intent.getIntExtra("iLockId", -1);
                int check_iErrorCode = intent.getIntExtra("iErrorCode", -1);
                if (check_iErrorCode < 0) {//命令执行失败，提示失败原因：线路连接、硬件故障等
                    String sErrordesc = intent.getStringExtra("sErrordesc");
                    Common.Door_status = 0;
                    Message msg = new Message();
                    msg.what = Constants.DOOR_STATE;
                    Common.mainActivityHandler.sendMessage(msg);
                } else {//命令执行成功，获取箱门当前状态
                    boolean bOpend = intent.getBooleanExtra("bOpend", false);
                    Common.Door_status = bOpend ? 1 : 0;
                    Message msg = new Message();
                    msg.what = Constants.DOOR_STATE;
                    Common.mainActivityHandler.sendMessage(msg);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 处理扫码结果
     */
    private void dealScan(String code) {
        try {
            JSONObject data = new JSONObject();
            Common.log.write("处理扫码结果：" + code);
            data.put("express_num", code);
            JSONObject jsonObject = Common.packageJsonData(Constants.DEVICE_SCAN_CLASS, Constants.DEVICE_SCAN_METHOD, data);
            if (Common.socket.isConnected() && !Common.socket.isClosed()) {
                Common.startLoad();
                Common.tcpSocket.setTcpListener(new TcpListener() {
                    @Override
                    public void receive(Object message) {
                        dealOpen(message);
                    }
                });
                Common.put.println(Common.encryptByDES(jsonObject.toString(), Constants.DES_KEY));
                Common.put.flush();
            } else {
                Common.sendError("网络连接失败，请等待，或者联系管理员");
            }
        } catch (Exception e) {
            Common.sendError("网络连接失败，请等待，或者联系管理员");
            e.printStackTrace();
        }
    }

    /**
     * 处理开柜信息
     *
     * @param message
     */
    public static void dealOpen(Object message) {
        JSONObject jsonObject = (JSONObject) message;
        try {
            //开柜完成
            Common.endLoad();
            if (jsonObject.getJSONObject("data").getBoolean("success")) {
                //板地址
                final int boardId = jsonObject.getJSONObject("data").getInt("lock_board_id");
                //锁地址
                int lockId = jsonObject.getJSONObject("data").getInt("lock_id");
                //锁编号
                int lockCode = jsonObject.getJSONObject("data").getInt("lock_code");
                //锁编号
                final int logId = jsonObject.getJSONObject("data").getInt("logId");
                Common.sendError("打开柜子");
                Common.confirm_LockBoardVsersion();//2次判断LockBoardVsersion
                Common.save("板子型号：" + Common.LockBoardVsersion + " boardId: " + boardId + " lockId " + lockId);//记录板子有关信息到文件中
                if (Common.LockBoardVsersion.equals(Constants.THIRD_BOX_NAME)) {
                    if (lockId == 22) {
                        lockId = 0;
                    }
                    Jubu.openBox(boardId, lockId);
                    //回复开柜信息
                    try {
                        JSONObject reply = new JSONObject();
                        reply.put("logId", logId);
                        Common.put.println(Common.encryptByDES(Common.packageJsonData(Constants.OPEN_GRID_REPLY_JSON_CLASS, Constants.OPEN_GRID_REPLY_JSON_METHOD, reply).toString(), Constants.DES_KEY));
                        Common.put.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //再次开柜
                    JSONObject grid_info = new JSONObject();
                    try {
                        grid_info.put("boardId", boardId);
                        grid_info.put("lockId", lockId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Common.openAgain(grid_info);
                } else {
                    final int finalLockId = lockId;
                    Common.device.openGrid(boardId, lockId, new OpenGridListener() {
                        @Override
                        public void openEnd() {
                            //回复开柜信息
                            try {
                                JSONObject reply = new JSONObject();
                                reply.put("logId", logId);
                                Common.put.println(Common.encryptByDES(Common.packageJsonData(Constants.OPEN_GRID_REPLY_JSON_CLASS, Constants.OPEN_GRID_REPLY_JSON_METHOD, reply).toString(), Constants.DES_KEY));
                                Common.put.flush();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            //再次开柜
                            JSONObject grid_info = new JSONObject();
                            try {
                                grid_info.put("boardId", boardId);
                                grid_info.put("lockId", finalLockId);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Common.openAgain(grid_info);
                        }
                    });
                }
            } else {
                Common.sendError(jsonObject.getJSONObject("data").getString("msg"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}