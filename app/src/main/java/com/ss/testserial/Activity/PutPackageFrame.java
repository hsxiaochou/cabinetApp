package com.ss.testserial.Activity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ss.testserial.Common.Common;
import com.ss.testserial.Common.Constants;
import com.ss.testserial.JNI.Jubu;
import com.ss.testserial.JNI.OpenGridListener;
import com.ss.testserial.JNI.Scanner;
import com.ss.testserial.R;
import com.ss.testserial.Runnable.TcpListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;

/**
 * Created by Listen on 2016/9/19.
 */
public class PutPackageFrame extends Fragment {
    private View view = null;
    private ImageView back = null;
    private LinearLayout grid_list = null;
    private EditText express = null;
    private EditText phone = null;
    private EditText phone2 = null;
    private KeyBoard keyBoard = null;
    private TextView contact_phone = null;
    private final int SCAN_RESULT_MESSAGE = 0x00;
    private long time = 0;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SCAN_RESULT_MESSAGE:
                    express.setText(msg.obj.toString());
                    break;
                default:
                    break;
            }
        }
    };
    private Button clear_all;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.frame_putpackage, container, false);
        Common.frame = "put";
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
        Common.putFrameHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case Constants.GET_GRID_LIST_MESSAGE:
                        JSONArray jsonArray = (JSONArray) msg.obj;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            try {
                                final int box_type = jsonArray.getJSONObject(i).getInt("box_type");
                                String box_name = jsonArray.getJSONObject(i).getString("box_name");
                                int box_num = jsonArray.getJSONObject(i).getInt("box_num");

                                //添加按钮
                                Button btn = new Button(view.getContext());
                                LinearLayout.LayoutParams btnLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                                btnLayout.setMargins(5, 5, 5, 5);
                                btn.setLayoutParams(btnLayout);
                                btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                                btn.setText(box_num + "个" + box_name);
                                if (box_num > 0) {
                                    btn.setEnabled(true);
                                    btn.setBackgroundResource(R.drawable.pink_radius);
                                    btn.setTextColor(Color.parseColor("#ffffff"));
                                    btn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if (canClick() && Common.isOpen) {
                                                putPackage(box_type);
                                            }
                                        }
                                    });
                                } else {
                                    btn.setEnabled(false);
                                    btn.setBackgroundResource(R.drawable.disabled_button);
                                    btn.setTextColor(Color.parseColor("#999999"));
                                }
                                grid_list.addView(btn);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case Constants.PUT_PACKAGE_SUCCESS_MESSAGE:
                        //初始化
                        express.setText("");
                        phone.setText("");
                        phone2.setText("");
                        express.requestFocus();
                        //重新获取剩余柜子
                        getGrid();
                        break;
                    default:
                        break;
                }
            }
        };
        //柜子列表
        this.grid_list = (LinearLayout) this.view.findViewById(R.id.grid_list);
        //返回按钮
        this.back = (ImageView) this.view.findViewById(R.id.back2main);
        this.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.wechat_id = "";
                Common.user_name = "";
                FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
                fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                MainFrame mainFrame = new MainFrame();
                fragmentTransaction.replace(R.id.content, mainFrame);
                fragmentTransaction.commitAllowingStateLoss();
            }
        });
        //快递单号
        this.express = (EditText) this.view.findViewById(R.id.express);
        Common.disableShowSoftInput(this.express);

//        this.express.setInputType(InputType.TYPE_NULL);     //my修改
        this.express.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                if (focus) {
                    express.setBackgroundResource(R.drawable.pink_border);
                    keyBoard.setView(express);
                } else {
                    express.setBackgroundResource(R.drawable.gray_border);
                }
            }
        });
        //用户电话
        this.phone = (EditText) this.view.findViewById(R.id.phone);
//        this.phone.setInputType(InputType.TYPE_NULL);
        Common.disableShowSoftInput(this.phone);
        this.phone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                if (focus) {
                    phone.setTransformationMethod(null);
                    phone.setBackgroundResource(R.drawable.pink_border);
                    keyBoard.setView(phone);
                } else {
                    phone.setTransformationMethod(new PasswordTransformationMethod());
                    phone.setBackgroundResource(R.drawable.gray_border);
                }
            }
        });
        //用户电话
        this.phone2 = (EditText) this.view.findViewById(R.id.phone2);
//        this.phone2.setInputType(InputType.TYPE_NULL);
        Common.disableShowSoftInput(this.phone2);
        this.phone2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                if (focus) {
                    phone2.setTransformationMethod(null);
                    phone2.setBackgroundResource(R.drawable.pink_border);
                    keyBoard.setView(phone2);
                } else {
                    phone2.setTransformationMethod(new PasswordTransformationMethod());
                    phone2.setBackgroundResource(R.drawable.gray_border);
                }
            }
        });
        //软键盘
        this.keyBoard = new KeyBoard(this.view, Constants.KEY_BOARD_NUM);

        //点击清除按钮
        this.clear_all = (Button) this.view.findViewById(R.id.clear_all);
        clear_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View myview) {
                EditText editText = (EditText) view.findFocus();
                editText.setText("");
            }
        });
        this.keyBoard.setKeyBoardListener(new KeyBoard.KeyBoardListener() {
            @Override
            public void delete() {
                try {
                    EditText editText = (EditText) view.findFocus();
                    int index = editText.getSelectionStart();
//                    int index = editText.getText().length();
                    if (index > 0) {
                        editText.getText().delete(index - 1, index);
                    }
                } catch (Exception e) {
                }
            }

            @Override
            public void input(String c) {
                try {
                    EditText editText = (EditText) view.findFocus();
//                    editText.append(c);
                    Common.insert(editText, c);
                } catch (Exception e) {
                }
            }
        });
        this.express.requestFocus();
        Common.confirm_LockBoardVsersion();//2次判断LockBoardVsersion
        if (!Common.LockBoardVsersion.equals(Constants.THIRD_BOX_NAME)) {
            //初始化扫码
            Common.device.scanner.setOnScanerReaded(new Scanner.ScannerInterface() {
                @Override
                public void onScanerReaded(String readData) {
                    Message message = new Message();
                    message.what = SCAN_RESULT_MESSAGE;
                    message.obj = readData;
                    handler.sendMessage(message);
                }
            });
            Common.device.scanner.open();
        }
        //获取柜子
        getGrid();
    }

    /**
     * 获取柜子
     */
    private void getGrid() {
        grid_list.removeAllViews();
        Common.startLoad();
        //从服务端获取柜子
        JSONObject getCodeJson = new JSONObject();
        try {
            Log.e("获取柜子", getCodeJson.toString());
            Common.put.println(Common.encryptByDES(Common.packageJsonData(Constants.GET_GRID_TYPE_CLASS, Constants.GET_GRID_TYPE_METHOD, getCodeJson).toString(), Constants.DES_KEY));
            Common.put.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 放包裹
     *
     * @param box_type 柜子类型
     */
    private void putPackage(int box_type) {
        Common.isOpen = false;
        String express_number = this.express.getText().toString();
        if (express_number.length() == 0) {
            Common.sendError("请输入快递单号");
            Common.isOpen = true;
            return;
        }
        String phone_number = this.phone.getText().toString();
        if (!Common.isPhone(phone_number)) {
            Common.sendError("请输入正确的手机号");
            Common.isOpen = true;
            return;
        }
        //判断两次输入电话是否一致
        if (!this.phone2.getText().toString().equals(phone_number)) {
            Common.sendError("两次输入手机号不一致");
            Common.isOpen = true;
            return;
        }
        Common.startLoad();
        JSONObject putJson = new JSONObject();
        try {
            putJson.put("box_type", box_type);
            putJson.put("express_num", express_number);
            putJson.put("user_phone", phone_number);
            putJson.put("user_identity", Common.wechat_id);
            Common.log.write("快递员投件：" + putJson.toString());
            Common.put.println(Common.encryptByDES(Common.packageJsonData(Constants.SEND_PACKAGE_JSON_CLASS, Constants.SEND_PACKAGE_JSON_METHOD, putJson).toString(), Constants.DES_KEY));
            Common.put.flush();
        } catch (Exception e) {
            Common.isOpen = true;
            e.printStackTrace();
        }
    }

    /**
     * 开柜成功回调
     */
    public static void putSuccess(final int boardId, int lockId, int lockCode, int logId) {
        final int _logId = logId;
        Common.endLoad();
        Common.sendError("开柜成功");
        // TODO:
        Common.confirm_LockBoardVsersion();//2次判断LockBoardVsersion

        Common.save("板子型号：" + Common.LockBoardVsersion + " boardId: " + boardId + " lockId " + lockId);//记录板子有关信息到文件中
        if (Common.LockBoardVsersion.equals(Constants.THIRD_BOX_NAME)) {
            lockId = Common.JUBU_ZeroId(lockId);
            Jubu.openBox(boardId, lockId);
            //回复开柜信息
            try {
                JSONObject reply = new JSONObject();
                reply.put("logId", _logId);
                Common.put.println(Common.encryptByDES(Common.packageJsonData(Constants.OPEN_GRID_REPLY_JSON_CLASS, Constants.OPEN_GRID_REPLY_JSON_METHOD, reply).toString(), Constants.DES_KEY));
                Common.put.flush();
                //开柜成功
                Message msg = new Message();
                msg.what = Constants.PUT_PACKAGE_SUCCESS_MESSAGE;
                msg.obj = "";
                Common.putFrameHandler.sendMessage(msg);
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
                reply.put("logId", _logId);
                Common.put.println(Common.encryptByDES(Common.packageJsonData(Constants.OPEN_GRID_REPLY_JSON_CLASS, Constants.OPEN_GRID_REPLY_JSON_METHOD, reply).toString(), Constants.DES_KEY));
                Common.put.flush();
                //开柜成功
                Message msg = new Message();
                msg.what = Constants.PUT_PACKAGE_SUCCESS_MESSAGE;
                msg.obj = "";
                Common.putFrameHandler.sendMessage(msg);
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

//            Common.device.openGrid(boardId, lockId, new OpenGridListener() {
//                @Override
//                public void openEnd() {
//                    //回复开柜信息
//                    try {
//                        JSONObject reply = new JSONObject();
//                        reply.put("logId", _logId);
//                        Common.put.println(Common.encryptByDES(Common.packageJsonData(Constants.OPEN_GRID_REPLY_JSON_CLASS, Constants.OPEN_GRID_REPLY_JSON_METHOD, reply).toString(), Constants.DES_KEY));
//                        Common.put.flush();
//                        //开柜成功
//                        Message msg = new Message();
//                        msg.what = Constants.PUT_PACKAGE_SUCCESS_MESSAGE;
//                        msg.obj = "";
//                        Common.putFrameHandler.sendMessage(msg);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    //再次开柜
//                    JSONObject grid_info = new JSONObject();
//                    try {
//                        grid_info.put("boardId", boardId);
//                        grid_info.put("lockId", finalLockId);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    Common.openAgain(grid_info);
//                }
//            });
        }
    }

    private boolean canClick() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.time < 1000) {
            return false;
        }
        this.time = currentTime;
        return true;
    }
}