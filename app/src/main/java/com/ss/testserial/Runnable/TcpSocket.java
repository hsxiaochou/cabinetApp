package com.ss.testserial.Runnable;

import android.app.AlertDialog;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.ss.testserial.Activity.GetFrame;
import com.ss.testserial.Activity.Layout3Frame;
import com.ss.testserial.Activity.MainActivity;
import com.ss.testserial.Activity.PutPackageFrame;
import com.ss.testserial.Activity.ScanFrame;
import com.ss.testserial.Activity.SendFrame;
import com.ss.testserial.Common.Common;
import com.ss.testserial.Common.Constants;
import com.ss.testserial.JNI.DeviceInterface;
import com.ss.testserial.JNI.Jubu;
import com.ss.testserial.JNI.OpenGridListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Listen on 2016/9/9.
 * 建立长连接线程
 */
public class TcpSocket implements Runnable {
    private String info = null;
    private TcpListener tcpListener;

    public void setTcpListener(TcpListener tcpListener) {
        this.tcpListener = tcpListener;
    }

    @Override
    public void run() {
        while (true) {
            //建立长连接
            try {
                if (Common.get != null) {
                    Common.get.close();
                }
                if (Common.put != null) {
                    Common.put.close();
                }
                if (Common.socket != null && !Common.socket.isClosed()) {
                    Common.socket.close();
                }
                Common.socket = new Socket(InetAddress.getByName(Constants.HOST), Constants.PORT);
                //Common.socket = new Socket(Constants.HOST,Constants.PORT);
                Common.socket.setSoTimeout(Constants.CONNECT_BLOCK_TIMEOUT);
                Common.get = new BufferedReader(new InputStreamReader(Common.socket.getInputStream()));
                Common.put = new PrintWriter(Common.socket.getOutputStream());
                Common.isBoardRegister = false;
                Common.log.write("网络连接成功");
                if (Common.IS_REGIST) {
                    Common.register();
                    Common.IS_REGIST = false;
                }
                Common.sendError("网络连接成功");
            } catch (IOException e) {
                e.printStackTrace();
                Common.log.write("网络连接失败：" + e.getMessage());
                Common.IS_REGIST = true;
                Common.sendError("网络连接失败，正在重新连接...");
                try {
                    Thread.sleep(Constants.RECONNECT_DELAY);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                continue;
            }
            while (!Common.socket.isClosed() && Common.socket.isConnected() && !Common.socket.isInputShutdown()) {
                try {
                    this.info = null;
                    this.info = Common.get.readLine();
                    if (this.info != null) {
                        this.dealInfo(this.info);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Common.log.write("网络断开");
                    Common.sendError("网络断开，正在重新连接...");
                    try {
                        Common.socket.close();
                        Thread.sleep(Constants.RECONNECT_DELAY);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 接收到消息时处理
     */
    private void dealInfo(String info) {
        try {
            Common.reboot_count_down = Constants.REBOOT_COUNT_DOWN;
            if (info.equals("1")) {
                // 回复心跳包
                Common.log.write("回复心跳包");
                Common.put.println(Common.encryptByDES(Common.packageJsonData(Constants.HEART_CLASS, Constants.HEART_METHOD, 1).toString(), Constants.DES_KEY));
                Common.put.flush();
                return;
            }
            String decryptByDES = Common.decryptByDES(info, Constants.DES_KEY);
            Common.log.write("接收数据：" + decryptByDES);
            final JSONObject jsonObject = new JSONObject(decryptByDES);
            //验证签名sign
            if (Common.Md5FromJson(jsonObject).equals(jsonObject.getString("sign"))) {
                Log.e("TAG", "开柜：" + jsonObject.toString());
                //获取数据,根据返回执行操作
                String classString = jsonObject.getString("class");
                String method = jsonObject.getString("method");
                //注册设备
                if (classString.equals(Constants.REGISTER_DEVICE_JSON_CLASS) && method.equals(Constants.REGISTER_DEVICE_JSON_METHOD)) {
                    if (jsonObject.getJSONObject("data").getBoolean("success")) {
                        Common.boxid = jsonObject.getJSONObject("data").getInt("boxId");
                        Common.contact_phone = jsonObject.getJSONObject("data").getString("phone");
                        Common.address = jsonObject.getJSONObject("data").getString("address");          // 新增
                        if (Common.contact_phone == "null") {
                            Common.contact_phone = "";
                        }
                        if (Common.address == "null") {          // 新增
                            Common.address = "";               // 新增
                        }                                      // 新增
                        Message message = new Message();
                        message.what = Constants.REGISTER_SUCCESS_MESSAGE;
                        Common.mainActivityHandler.sendMessage(message);
                        Common.log.write("注册设备成功");
                    } else {
                        Common.register();
                        Common.log.write("注册设备失败：" + jsonObject.toString());
                    }
                    //上报状态
                } else if (classString.equals(Constants.LOCK_STATUS_JSON_CLASS) && method.equals(Constants.LOCK_STATUS_JSON_METHOD)) {
                    Common.log.write("返回上报状态");
                    //开柜
                } else if (classString.equals(Constants.OPEN_GRID_JSON_CLASS) && method.equals(Constants.OPEN_GRID_JSON_METHOD)) {


                    if (jsonObject.getJSONObject("data").getBoolean("success")) {
                        //板地址
                        final int boardId = jsonObject.getJSONObject("data").getInt("lock_board_id");
                        //锁地址
                        int lockId = jsonObject.getJSONObject("data").getInt("lock_id");
                        //锁编号
                        int lockCode = jsonObject.getJSONObject("data").getInt("lock_code");
                        //logId
                        final int logId = jsonObject.getJSONObject("data").getInt("logId");
                        Common.backToMain();
                        // TODO:
                        Common.confirm_LockBoardVsersion();//2次判断LockBoardVsersion

                        Common.save("板子型号：" + Common.LockBoardVsersion + " boardId: " + boardId + " lockId " + lockId);//记录板子有关信息到文件中
                        if (Common.LockBoardVsersion.equals(Constants.THIRD_BOX_NAME)) {
                            if (lockId == 22) {
                                lockId = 0;
                            }
                            Jubu.openBox(boardId, lockId);
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
                    //取件开柜
                } else if (classString.equals(Constants.GET_PACKAGE_JSON_CLASS) && method.equals(Constants.GET_PACKAGE_JSON_METHOD)) {
                    Common.log.write("返回取件开柜：" + jsonObject.toString());
                    GetFrame.dealOpen(jsonObject);
                    //快递员登录
                } else if (classString.equals(Constants.LOGIN_JSON_CLASS) && method.equals(Constants.LOGIN_JSON_METHOD)) {
                    Common.log.write("返回快递员登录：" + jsonObject.toString());
                    Common.endLoad();
                    try {
                        if (jsonObject.getJSONObject("data").getBoolean("success")) {
                            Common.wechat_id = jsonObject.getJSONObject("data").getString("user_identity");
                            Common.user_name = jsonObject.getJSONObject("data").getString("user_name");
                            Layout3Frame.loginSuccess();
                        } else {
                            Common.sendError(jsonObject.getJSONObject("data").getString("msg"));
                        }
                    } catch (Exception e) {
                    }
                    //预约投件
                } else if (classString.equals(Constants.CODE_SEND_PACKAGE_JSON_CLASS) && method.equals(Constants.CODE_SEND_PACKAGE_JSON_METHOD)) {
                    Common.log.write("返回投件码投件：" + jsonObject.toString());
                    SendFrame.dealOpen(jsonObject);
                } else if (classString.equals(Constants.DEVICE_SCAN_CLASS) && method.equals(Constants.DEVICE_SCAN_METHOD)) {
                    Common.log.write("返回扫码开柜：" + jsonObject.toString());
                    ScanFrame.dealOpen(jsonObject);
                    //获取柜子类型
                } else if (classString.equals(Constants.GET_GRID_TYPE_CLASS) && method.equals(Constants.GET_GRID_TYPE_METHOD)) {
                    Common.endLoad();
                    Log.e("TAG", jsonObject.toString());
                    Common.log.write("返回获取柜子类型：" + jsonObject.toString());
                    try {
                        if (jsonObject.getJSONObject("data").getBoolean("success")) {
                            if (Common.frame == "main") {                                         // 修改
                                Message msg = new Message();
                                msg.what = Constants.HOME_GET_GRID_LIST_MESSAGE;
                                msg.obj = jsonObject.getJSONObject("data").getJSONArray("list");
                                Common.mainActivityHandler.sendMessage(msg);
                            }
                            if (Common.frame == "put") {
                                Message msg = new Message();
                                msg.what = Constants.GET_GRID_LIST_MESSAGE;
                                msg.obj = jsonObject.getJSONObject("data").getJSONArray("list");
                                Common.putFrameHandler.sendMessage(msg);
                            }                                                                   // 修改
                        } else {
                            Common.sendError(jsonObject.getJSONObject("data").getString("msg"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //快递员投件
                } else if (classString.equals(Constants.SEND_PACKAGE_JSON_CLASS) && method.equals(Constants.SEND_PACKAGE_JSON_METHOD)) {
                    Common.log.write("返回快递员现场投件开柜：" + jsonObject.toString());
                    try {
                        if (jsonObject.getJSONObject("data").getBoolean("success")) {
                            //板地址
                            int boardId = jsonObject.getJSONObject("data").getInt("lock_board_id");
                            //锁地址
                            int lockId = jsonObject.getJSONObject("data").getInt("lock_id");
                            //锁编号
                            int lockCode = jsonObject.getJSONObject("data").getInt("lock_code");
                            //logId
                            final int logId = jsonObject.getJSONObject("data").getInt("logId");
                            PutPackageFrame.putSuccess(boardId, lockId, lockCode, logId);
                        } else {
                            Common.sendError(jsonObject.getJSONObject("data").getString("msg"));
                            Common.endLoad();
                            //初始化
                            Message msg = new Message();
                            msg.what = Constants.PUT_PACKAGE_SUCCESS_MESSAGE;
                            msg.obj = "";
                            Common.putFrameHandler.sendMessage(msg);
                        }
                    } catch (Exception e) {
                    }
                    //获取登录码
                } else if (classString.equals(Constants.GET_LOGIN_CODE_CLASS) && method.equals(Constants.GET_LOGIN_CODE_METHOD)) {
                    Common.endLoad();
                    try {
                        if (jsonObject.getJSONObject("data").getBoolean("success")) {
                            Layout3Frame.getLoginCodeSuccess();
                        } else {
                            Common.sendError(jsonObject.getJSONObject("data").getString("msg"));
                        }
                    } catch (Exception e) {
                    }
                    //出错
                } else if (classString.equals(Constants.UPDATE_CLASS) && method.equals(Constants.UPDATE_METHOD)) {
                    if (jsonObject.getJSONObject("data").getString("version").equals(Common.version)) {
                        Common.log.write("尝试升级但是版本一致");
                    } else {
                        //升级
                        Common.log.write("升级：" + jsonObject.getJSONObject("data").getString("version"));
                        Common.update.update(jsonObject.getJSONObject("data").getString("url"));
                    }
                } else {
                    Common.log.write("未知操作：[class:" + classString + "][method:" + method + "]");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
