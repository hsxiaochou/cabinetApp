package com.ss.testserial.Runnable;

import android.app.AlertDialog;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.MyApplication;
import com.ss.testserial.Activity.GetFrame;
import com.ss.testserial.Activity.Layout3Frame;
import com.ss.testserial.Activity.MainActivity;
import com.ss.testserial.Activity.MainFrame;
import com.ss.testserial.Activity.PutPackageFrame;
import com.ss.testserial.Activity.ScanFrame;
import com.ss.testserial.Activity.SendFrame;
import com.ss.testserial.Common.Common;
import com.ss.testserial.Common.Constants;
import com.ss.testserial.JNI.DeviceInterface;
import com.ss.testserial.JNI.Jubu;
import com.ss.testserial.JNI.OpenGridListener;
import com.ss.testserial.JNI.UartComm;
import com.ss.testserial.R;

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
                Log.e("TAG", e.toString());
                Common.log.write("网络连接失败，正在重新连接..." + e.getMessage());
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
                    Common.IS_REGIST = true;
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
            Log.e("TAG", info);
            Common.reboot_count_down = Constants.REBOOT_COUNT_DOWN;
            if (info.equals("1")) {
                // 回复心跳包
//                Common.log.write("回复心跳包");
                Common.put.println(Common.encryptByDES(Common.packageJsonData(Constants.HEART_CLASS, Constants.HEART_METHOD, 1).toString(), Constants.DES_KEY));
                Common.put.flush();
                return;
            }
            String decryptByDES = Common.decryptByDES(info, Constants.DES_KEY);
//            Common.log.write("接收数据：" + decryptByDES);
            final JSONObject jsonObject = new JSONObject(decryptByDES);
            //验证签名sign
            if (Common.Md5FromJson(jsonObject).equals(jsonObject.getString("sign"))) {
                Log.e("TAG", "接受的数据：" + jsonObject.toString());
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
                        if (Common.address == "null") {         // 新增
                            Common.address = "";               // 新增
                        }                                      // 新增
                        Message message = Common.mainActivityHandler.obtainMessage();
                        message.what = Constants.REGISTER_SUCCESS_MESSAGE;
                        message.sendToTarget();
                        Common.log.write("注册设备成功");
                    } else {
                        Common.register();
                        Common.log.write("注册设备失败：" + jsonObject.toString());
                    }
                    //上报状态
                } else if (classString.equals(Constants.LOCK_STATUS_JSON_CLASS) && method.equals(Constants.LOCK_STATUS_JSON_METHOD)) {
                    Common.log.write("返回上报状态");
                    //开柜
                } else if ((classString.equals(Constants.OPEN_GRID_JSON_CLASS) && method.equals(Constants.OPEN_GRID_JSON_METHOD)) || (classString.equals(Constants.CONFIRM_CLASS) && method.equals(Constants.CONFIRM_METHOD))) {
                    Common.endLoad();
                    if (jsonObject.getJSONObject("data").getBoolean("success")) {
                        //板地址
                        final int boardId = jsonObject.getJSONObject("data").getInt("lock_board_id");
                        //锁地址
                        int lockId = jsonObject.getJSONObject("data").getInt("lock_id");
                        //锁编号
                        int lockCode = jsonObject.getJSONObject("data").getInt("lock_code");
                        //logId
                        final int logId = jsonObject.getJSONObject("data").getInt("logId");
                        if (classString.equals(Constants.OPEN_GRID_JSON_CLASS) && method.equals(Constants.OPEN_GRID_JSON_METHOD)) {
                            Common.backToMain();
                        }
                        if (jsonObject.getJSONObject("data").has("is_weixin") && jsonObject.getJSONObject("data").has("package_id")) {
                            //再次开柜
                            JSONObject grid_info = new JSONObject();
                            try {
                                Common.frame = "determine";
                                grid_info.put("boardId", boardId);
                                grid_info.put("lockId", lockId);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Common.package_id = jsonObject.getJSONObject("data").getString("package_id");
                            JSONObject reply = new JSONObject();
                            reply.put("logId", logId);
                            // TODO:
                            Common.confirm_LockBoardVsersion();//2次判断LockBoardVsersion
                            Common.save("板子型号：" + Common.LockBoardVsersion + " boardId: " + boardId + " lockId " + lockId);//记录板子有关信息到文件中
                            if (Common.LockBoardVsersion.equals(Constants.THIRD_BOX_NAME)) {
                                lockId = Common.JUBU_ZeroId(lockId);
                                //回复开柜信息
                                try {
                                    Common.put.println(Common.encryptByDES(Common.packageJsonData(Constants.OPEN_GRID_REPLY_JSON_CLASS, Constants.OPEN_GRID_REPLY_JSON_METHOD, reply).toString(), Constants.DES_KEY));
                                    Common.put.flush();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                Jubu.openBox(boardId, lockId);
                            } else {
                                try {
                                    Common.put.println(Common.encryptByDES(Common.packageJsonData(Constants.OPEN_GRID_REPLY_JSON_CLASS, Constants.OPEN_GRID_REPLY_JSON_METHOD, reply).toString(), Constants.DES_KEY));
                                    Common.put.flush();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                Common.oPenDoor(boardId, lockId);
                            }
                            Common.Determine(grid_info);
                            Common.sendError("开柜成功");
                        } else {
                            // TODO:
                            Common.save("板子型号：" + Common.LockBoardVsersion + "开锁信息：" + jsonObject.getJSONObject("data").toString());//记录板子有关信息到文件中
                            if (Common.LockBoardVsersion.equals(Constants.THIRD_BOX_NAME)) {
                                if (lockId == 22) {
                                    lockId = 0;
                                }
                                try {
                                    JSONObject reply = new JSONObject();
                                    reply.put("logId", logId);
                                    Common.put.println(Common.encryptByDES(Common.packageJsonData(Constants.OPEN_GRID_REPLY_JSON_CLASS, Constants.OPEN_GRID_REPLY_JSON_METHOD, reply).toString(), Constants.DES_KEY));
                                    Common.put.flush();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                Jubu.openBox(boardId, lockId);
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
                                try {
                                    JSONObject reply = new JSONObject();
                                    reply.put("logId", logId);
                                    Common.put.println(Common.encryptByDES(Common.packageJsonData(Constants.OPEN_GRID_REPLY_JSON_CLASS, Constants.OPEN_GRID_REPLY_JSON_METHOD, reply).toString(), Constants.DES_KEY));
                                    Common.put.flush();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                Common.oPenDoor(boardId, lockId);
                                //再次开柜
                                JSONObject grid_info = new JSONObject();
                                try {
                                    grid_info.put("boardId", boardId);
                                    grid_info.put("lockId", finalLockId);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Common.openAgain(grid_info);
//                                Common.device.openGrid(boardId, lockId, new OpenGridListener() {
//                                    @Override
//                                    public void openEnd() {
//                                        try {
//                                            JSONObject reply = new JSONObject();
//                                            reply.put("logId", logId);
//                                            Common.put.println(Common.encryptByDES(Common.packageJsonData(Constants.OPEN_GRID_REPLY_JSON_CLASS, Constants.OPEN_GRID_REPLY_JSON_METHOD, reply).toString(), Constants.DES_KEY));
//                                            Common.put.flush();
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }
//                                        //再次开柜
//                                        JSONObject grid_info = new JSONObject();
//                                        try {
//                                            grid_info.put("boardId", boardId);
//                                            grid_info.put("lockId", finalLockId);
//                                        } catch (JSONException e) {
//                                            e.printStackTrace();
//                                        }
//                                        Common.openAgain(grid_info);
//                                    }
//                                });
                            }
                        }
                    } else {
                        Common.save("板子型号：" + Common.LockBoardVsersion + "开锁信息失败： " + jsonObject.getJSONObject("data").getString("msg"));
                        Common.sendError(jsonObject.getJSONObject("data").getString("msg"));
                    }
                    //取件开柜
                } else if (classString.equals(Constants.GET_PACKAGE_JSON_CLASS) && method.equals(Constants.GET_PACKAGE_JSON_METHOD)) {
                    Common.save("板子型号：" + Common.LockBoardVsersion + "返回取件开柜： " + jsonObject.toString());
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
                            Common.overdue = jsonObject.getJSONObject("data").getBoolean("overdue");
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
                } else if (classString.equals(Constants.CODE_SEND_PACKAGE_JSON_CLASS) && method.equals(Constants.CODE_SEND_PACKAGE_JSON_METHOD_2)) {
                    Common.log.write("返回投件码投件：" + jsonObject.toString());
                    SendFrame.dealOpen(jsonObject);
                } else if (classString.equals(Constants.DEVICE_SCAN_CLASS) && method.equals(Constants.DEVICE_SCAN_METHOD)) {
                    Common.log.write("返回扫码开柜：" + jsonObject.toString());
                    ScanFrame.dealOpen(jsonObject);
                    //获取柜子类型
                } else if (classString.equals(Constants.GET_GRID_TYPE_CLASS) && method.equals(Constants.GET_GRID_TYPE_METHOD)) {
                    Common.isOpen = true;
                    Common.endLoad();
                    Common.log.write("返回获取柜子类型：" + jsonObject.toString());
                    try {
                        if (jsonObject.getJSONObject("data").getBoolean("success")) {
                            if (Common.frame == "main") {                                         // 修改
                                Message msg = Common.mainActivityHandler.obtainMessage();
                                msg.what = Constants.HOME_GET_GRID_LIST_MESSAGE;
                                msg.obj = jsonObject.getJSONObject("data").getJSONArray("list");
                                msg.sendToTarget();
                            }
                            if (Common.frame == "put") {
                                Message msg = Common.putFrameHandler.obtainMessage();
                                msg.what = Constants.GET_GRID_LIST_MESSAGE;
                                msg.obj = jsonObject.getJSONObject("data").getJSONArray("list");
                                msg.sendToTarget();
                            }                                                                   // 修改
                        } else {
                            Common.sendError(jsonObject.getJSONObject("data").getString("msg"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //快递员投件
                } else if (classString.equals(Constants.SEND_PACKAGE_JSON_CLASS) && method.equals(Constants.SEND_PACKAGE_JSON_METHOD)) {
                    Common.isOpen = true;
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
                            Message msg = Common.putFrameHandler.obtainMessage();
                            msg.what = Constants.PUT_PACKAGE_SUCCESS_MESSAGE;
                            msg.obj = "";
                            msg.sendToTarget();
                        }
                    } catch (Exception e) {
                    }
                    //获取登录码
                } else if (classString.equals(Constants.SEND_PACKAGE_JSON_CLASS) && method.equals(Constants.SEND_PACKAGE_JSON_METHOD2)) {
                    //修改的投件方法
                    Common.isOpen = true;
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
                            Common.package_id = jsonObject.getJSONObject("data").getString("package_id");
                            PutPackageFrame.putSuccess(boardId, lockId, lockCode, logId);
                        } else {
                            Common.sendError(jsonObject.getJSONObject("data").getString("msg"));
                            Common.endLoad();
                            //初始化
                            Message msg = Common.putFrameHandler.obtainMessage();
                            msg.what = Constants.PUT_PACKAGE_SUCCESS_MESSAGE;
                            msg.obj = "";
                            msg.sendToTarget();
                        }
                    } catch (Exception e) {

                    }

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
                } else if (classString.equals(Constants.VOLCLASS) && method.equals(Constants.VOLMETHOD)) {
                    final String volumn = jsonObject.getJSONObject("data").getString("volumn");
                    Common.getUpVolume(Integer.parseInt(volumn));
                } else if (classString.equals(Constants.QUERY_CLASS) && method.equals(Constants.QUERY_METHOD)) {
                    //自助查询信息返回
                    Common.endLoad();
                    //初始化
                    Message msg = Common.queryFrameHandler.obtainMessage();
                    msg.what = Constants.QUERY_INFO;
                    msg.obj = jsonObject.toString();
                    msg.sendToTarget();
                } else if (classString.equals(Constants.GETQUERY_CODE_CLASS) && method.equals(Constants.GETQUERY_CODE_METHOD)) {
                    //一键发送短信的信息返回
                    Common.endLoad();
                    Message msg = Common.queryFrameHandler.obtainMessage();
                    msg.what = Constants.QUERY_SEND_INFO;
                    msg.obj = jsonObject.toString();
                    msg.sendToTarget();

                } else if (classString.equals(Constants.LOG_UP_CLASS) && method.equals(Constants.LOG_UP_METHOD)) {
                    //回收日志
                    Common.uploadHttpURLConnection(Constants.path + "data.txt");
                } else if (classString.equals(Constants.CLEAR_LOG_CLASS) && method.equals(Constants.CLEAR_LOG_METHOD)) {
                    //清理日志
                    if (TextUtils.isEmpty(Common.CrashLogName)) {
                        Common.ClearLog();
                    }
                } else if (classString.equals(Constants.REBOOT_CLASS) && method.equals(Constants.REBOOT_METHOD)) {
                    //重启系统
                    Common.save("  远程重启");
                    Common.rebot();
                } else if (classString.equals(Constants.GETVIDEO_CLASS) && method.equals(Constants.GETVIDEO_METHOD)) {
                    //获取视频json
                    Message msg = Common.mainActivityHandler.obtainMessage();
                    msg.what = Constants.GET_VIDEO;
                    msg.obj = jsonObject.toString();
                    msg.sendToTarget();
                } else if (classString.equals(Constants.GET_GRID_TYPE_CLASS) && method.equals(Constants.SENDMSG)) {

                    Common.endLoad();
                    if (jsonObject.getJSONObject("data").getBoolean("success")) {
                        Common.sendError(jsonObject.getJSONObject("data").getString("msg"));
                    }
                    if (Common.count_down > 0) {
                        Common.determineFrameHandler.sendEmptyMessage(0);
                    }
                } else if (classString.equals(Constants.GET_GRID_TYPE_CLASS) && method.equals(Constants.RESETLOCK)) {
                    Common.endLoad();
                    if (jsonObject.getJSONObject("data").getBoolean("success")) {
                        Common.sendError(jsonObject.getJSONObject("data").getString("msg"));
                    }
                    Common.determineFrameHandler.sendEmptyMessage(0);
                } else if (classString.equals(Constants.GETCOURIER_CLASS) && method.equals(Constants.GETCOURIER_METHOD)) {
                    //获取快递员回收json
                    Common.endLoad();
                    if (jsonObject.getJSONObject("data").getBoolean("success")) {
                        Message msg = Common.RecyclingFrameHandler.obtainMessage();
                        msg.what = Constants.GET_COURIER;
                        msg.obj = jsonObject.toString();
                        msg.sendToTarget();
                    }

                } else {
                    Common.save("未知操作：[class:" + classString + "][method:" + method + "]");
                    Common.log.write("未知操作：[class:" + classString + "][method:" + method + "]" + "未知信息：" + jsonObject.toString());
                }
            }
        } catch (final Exception e) {
            Common.save(e.toString());
            if (Common.mainActivity != null) {
                Common.mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Common.mainActivity, "异常：" + e.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            e.printStackTrace();
        }
    }
}
