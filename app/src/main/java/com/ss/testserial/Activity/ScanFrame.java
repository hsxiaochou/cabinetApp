package com.ss.testserial.Activity;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.text.LoginFilter;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ss.testserial.Common.Common;
import com.ss.testserial.Common.Constants;
import com.ss.testserial.Common.QRCode;
import com.ss.testserial.JNI.Jubu;
import com.ss.testserial.JNI.OpenGridListener;
import com.ss.testserial.JNI.Scanner;
import com.ss.testserial.R;
import com.ss.testserial.Runnable.TcpListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;

/**
 * Created by Listen on 2016/9/19.
 */
public class ScanFrame extends Fragment {
    private View view = null;
    private ImageView back = null;
    private TextView express = null;
    private TextView contact_phone = null;

    private final int SCAN_RESULT_MESSAGE = 0x00;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SCAN_RESULT_MESSAGE:
                    express.setText(msg.obj.toString());
                    dealScan(msg.obj.toString());
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.scan_putpackage, container, false);
        Common.frame = "scaner";
        Common.log.write("进入扫码界面");
        this.init();
        return this.view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);
        } catch (NoSuchFieldException e1) {
            throw new RuntimeException(e1);
        } catch (IllegalAccessException e2) {
            throw new RuntimeException(e2);
        }
    }

    /**
     * 初始化
     */
    private void init() {
        this.contact_phone = (TextView) this.view.findViewById(R.id.contact_phone);
        this.contact_phone.setText(Common.contact_phone);
        //快递单号
        this.express = (TextView) this.view.findViewById(R.id.express);
        //返回按钮
        this.back = (ImageView) this.view.findViewById(R.id.back2layout2);
        this.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Common.device.scanner.close();
                } catch (Exception e) {
                    Log.e("TAG", e.toString());
                }

                FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
                SendFrame sendFrame = new SendFrame();
                fragmentTransaction.replace(R.id.content, sendFrame);
                Common.log.write("点击扫码界面返回按钮");
                fragmentTransaction.commitAllowingStateLoss();
            }
        });
        //初始化
        if (Common.device.scanner != null) {
            Common.device.scanner.setOnScanerReaded(new Scanner.ScannerInterface() {
                @Override
                public void onScanerReaded(String readData) {
                    Message message = new Message();
                    message.what = SCAN_RESULT_MESSAGE;
                    message.obj = readData;
                    handler.sendMessage(message);
                }
            });
            Common.confirm_LockBoardVsersion();//2次判断LockBoardVsersion
            if (!Common.LockBoardVsersion.equals(Constants.THIRD_BOX_NAME)) {
                Common.device.scanner.open();
            }
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
                // TODO:
                Common.confirm_LockBoardVsersion();//2次判断LockBoardVsersion
                Common.save("板子型号：" + Common.LockBoardVsersion + " boardId: " + boardId + " lockId " + lockId);//记录板子有关信息到文件中
                if (Common.LockBoardVsersion.equals(Constants.THIRD_BOX_NAME)) {
                    lockId=Common.JUBU_ZeroId(lockId);
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
