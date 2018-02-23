package com.ss.testserial.Activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ss.testserial.Common.Common;
import com.ss.testserial.Common.Constants;
import com.ss.testserial.Common.QRCode;
import com.ss.testserial.JNI.Jubu;
import com.ss.testserial.JNI.OpenGridListener;
import com.ss.testserial.JNI.UartComm;
import com.ss.testserial.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;

/**
 * Created by Listen on 2016/9/19.
 */
public class GetFrame extends Fragment {
    private View view = null;
    private ImageView back = null;
    private EditText[] code = null;
    private TextView contact_phone = null;
    private int position = 0;
    private KeyBoard keyBoard;
    private LayoutInflater inflater;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        this.view = inflater.inflate(R.layout.frame_layout2_get, container, false);
        Common.frame = "get";
        this.init();
        //生成取件二维码
        ImageView getcode = (ImageView) view.findViewById(R.id.getcode);
        getcode.setImageBitmap(QRCode.makeQrcode(Constants.DOMAIN + "/index.php?m=App&c=Scan&a=packageList&type=get&boxid=" + Common.boxid + "&timespan=" + System.currentTimeMillis(), 800));
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
        Common.getFrameHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case Constants.GET_PACKAGE_SUCCESS_MESSAGE:
                        //初始化
                        keyBoard.setView(code[0]);
                        position = 0;
                        for (int i = 0; i < code.length; i++) {
                            code[i].setText("");
                        }
                        break;
                    case Constants.GET_PACKAGE_ERROR_MESSAGE:
                        Common.pay_qrcode = msg.obj.toString();
                        if (getActivity() != null) {
                            FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
                            PayQrcodeFrame payQrcodeFrame = new PayQrcodeFrame();
                            fragmentTransaction.replace(R.id.content, payQrcodeFrame);
                            fragmentTransaction.commitAllowingStateLoss();
                        }
                        break;
                    default:
                        break;
                }
            }
        };
        //返回按钮
        this.back = (ImageView) this.view.findViewById(R.id.back2layout1);
        this.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //初始化
                keyBoard.setView(code[0]);
                position = 0;
                for (int i = 0; i < code.length; i++) {
                    code[i].setText("");
                }
                FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
                fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                Common.frame = "main";
                MainFrame mainFrame = new MainFrame();
                fragmentTransaction.replace(R.id.content, mainFrame);
                fragmentTransaction.commitAllowingStateLoss();
            }
        });
        //验证码
        this.code = new EditText[6];
        this.code[0] = (EditText) this.view.findViewById(R.id.getCode1);
        this.code[1] = (EditText) this.view.findViewById(R.id.getCode2);
        this.code[2] = (EditText) this.view.findViewById(R.id.getCode3);
        this.code[3] = (EditText) this.view.findViewById(R.id.getCode4);
        this.code[4] = (EditText) this.view.findViewById(R.id.getCode5);
        this.code[5] = (EditText) this.view.findViewById(R.id.getCode6);
        //软键盘
        this.keyBoard = new KeyBoard(this.view, Constants.KEY_BOARD_NUM);
        this.keyBoard.setKeyBoardListener(new KeyBoard.KeyBoardListener() {
            @Override
            public void delete() {
                //出错清空
                if (position >= code.length) {
                    //初始化
                    keyBoard.setView(code[0]);
                    position = 0;
                    for (int i = 0; i < code.length; i++) {
                        code[i].setText("");
                    }
                    return;
                }

                if (code[position].getText().length() == 0 && position > 0) {
                    position--;
                    keyBoard.setView(code[position]);
                    code[position].setText("");
                } else {
                    code[position].setText("");
                }
            }

            @Override
            public void input(String c) {
                if (position < code.length) {
                    code[position].setText(c);
                }
                position++;
                if (position < code.length) {
                    keyBoard.setView(code[position]);
                }
                if (position == code.length) {
                    getPackage();
                }
                //出错清空
                if (position > code.length) {
                    //初始化
                    keyBoard.setView(code[0]);
                    position = 0;
                    for (int i = 0; i < code.length; i++) {
                        code[i].setText("");
                    }
                }
            }
        });
        this.keyBoard.setView(this.code[0]);
        for (int i = 0; i < this.code.length; i++) {
            this.code[i].setInputType(InputType.TYPE_NULL);
        }
    }

    /**
     * 发送取件信息
     */
    private void getPackage() {
        //获取验证码
        String code = "";
        for (int i = 0; i < this.code.length; i++) {
            code += this.code[i].getText();
        }
        try {
            JSONObject data = new JSONObject();
            data.put("verify_code", code);
            data.put("mac", Common.mac);
            JSONObject jsonObject = Common.packageJsonData(Constants.GET_PACKAGE_JSON_CLASS, Constants.GET_PACKAGE_JSON_METHOD, data);
            if (Common.socket.isConnected() && !Common.socket.isClosed()) {
                Common.log.write("发送取件码：" + code);
                Common.put.println(Common.encryptByDES(jsonObject.toString(), Constants.DES_KEY));
                Common.put.flush();
                Common.startLoad();
            } else {
                Common.sendError("网络连接失败，请稍后再试");
            }
        } catch (Exception e) {
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
            Message msg = new Message();
            msg.what = Constants.GET_PACKAGE_SUCCESS_MESSAGE;
            msg.obj = "";
            Common.getFrameHandler.sendMessage(msg);
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
                Common.sendError("将打开" + lockCode + "号柜");
                Common.confirm_LockBoardVsersion();//2次判断LockBoardVsersion
                Common.save("取件开柜   " + "板子型号：" + Common.LockBoardVsersion + " boardId: " + boardId + " lockId " + lockId);//记录板子有关信息到文件中
                // TODO:
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
                    Common.oPenDoor(boardId, lockId);
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
//                    Common.device.openGrid(boardId, lockId, new OpenGridListener() {
//                        @Override
//                        public void openEnd() {
//                            //回复开柜信息
//                            try {
//                                JSONObject reply = new JSONObject();
//                                reply.put("logId", logId);
//                                Common.put.println(Common.encryptByDES(Common.packageJsonData(Constants.OPEN_GRID_REPLY_JSON_CLASS, Constants.OPEN_GRID_REPLY_JSON_METHOD, reply).toString(), Constants.DES_KEY));
//                                Common.put.flush();
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                            //再次开柜
//                            JSONObject grid_info = new JSONObject();
//                            try {
//                                grid_info.put("boardId", boardId);
//                                grid_info.put("lockId", finalLockId);
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                            Common.openAgain(grid_info);
//                        }
//                    });
                }
            } else {
                try {
                    Common.save("取件失败：" + jsonObject.getJSONObject("data").getString("msg"));//记录板子有关信息到文件中
                    //取件超时
                    String url = jsonObject.getJSONObject("data").getString("payUrl");
                    Message message1 = new Message();
                    message1.obj = url;
                    message1.what = Constants.GET_PACKAGE_ERROR_MESSAGE;
                    Common.getFrameHandler.sendMessage(message1);
                } catch (Exception e) {
                    Common.save("取件异常");//记录板子有关信息到文件中
                    Common.sendError(jsonObject.getJSONObject("data").getString("msg"));
                }
            }
        } catch (Exception e) {
            Common.save("取件异常");
        }
    }
}
