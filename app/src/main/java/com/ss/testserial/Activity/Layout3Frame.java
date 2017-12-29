package com.ss.testserial.Activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.ss.testserial.Common.Common;
import com.ss.testserial.Common.Constants;
import com.ss.testserial.R;
import com.ss.testserial.Runnable.TcpListener;

import org.json.JSONObject;

import java.lang.reflect.Field;

/**
 * Created by Listen on 2016/9/19.
 */
public class Layout3Frame extends Fragment {
    private View view = null;
    private EditText phone = null;
    private EditText loginCode = null;
    private ImageView back = null;
    private Button getLoginCode = null;
    private Button login = null;
    private KeyBoard keyBoard;
    PutPackageFrame putPackageFrame = new PutPackageFrame();
    //获取验证码倒计时
    public static int countDown = 150;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.frame_layout3, container, false);
        Common.frame = "login";
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
        Common.loginFrameHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case Constants.DISABLED_BUTTON_MESSAGE:
                        getLoginCode.setEnabled(false);
                        getLoginCode.setTextColor(Color.parseColor("#999999"));
                        getLoginCode.setBackgroundResource(R.drawable.disabled_button);
                        break;
                    case Constants.COUNT_DOWN_BUTTON_MESSAGE:
                        getLoginCode.setText(Layout3Frame.countDown + "秒");
                        break;
                    case Constants.ABLED_DOWN_BUTTON_MESSAGE:
                        getLoginCode.setText("获取登录码");
                        getLoginCode.setEnabled(true);
                        getLoginCode.setTextColor(Color.parseColor("#ffffff"));
                        getLoginCode.setBackgroundResource(R.drawable.pink_radius);
                        break;
                    case Constants.LOGIN_SUCCESS_MESSAGE:
                        Common.sendError("登录成功");
                        FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.content, putPackageFrame);
                        fragmentTransaction.commitAllowingStateLoss();
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
                FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
                fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                MainFrame mainFrame = new MainFrame();
                fragmentTransaction.replace(R.id.content, mainFrame);
                fragmentTransaction.commitAllowingStateLoss();
            }
        });
        //手机号
        this.phone = (EditText) this.view.findViewById(R.id.phone);
//        this.phone.setInputType(InputType.TYPE_NULL);
//        this.phone.setTextIsSelectable(false);
//        this.phone.setLongClickable(false);
        Common.disableShowSoftInput(this.phone);            //my修改

        this.phone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                if (focus) {
                    phone.setBackgroundResource(R.drawable.pink_border);
                    keyBoard.setView(phone);
                } else {
                    phone.setBackgroundResource(R.drawable.gray_border);
                }
            }
        });
        //验证码
        this.loginCode = (EditText) this.view.findViewById(R.id.loginCode);
        this.loginCode.setInputType(InputType.TYPE_NULL);
        this.loginCode.setTransformationMethod(new PasswordTransformationMethod());

        this.loginCode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                if (focus) {
                    loginCode.setBackgroundResource(R.drawable.pink_border);
                    keyBoard.setView(loginCode);
                } else {
                    loginCode.setBackgroundResource(R.drawable.gray_border);
                }
            }
        });
        //获取验证码
        this.getLoginCode = (Button) this.view.findViewById(R.id.getLoginCode);
        getLoginCode.setEnabled(false);
        getLoginCode.setTextColor(Color.parseColor("#999999"));
        getLoginCode.setBackgroundResource(R.drawable.disabled_button);
        this.getLoginCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String number = phone.getText().toString();
                if (Common.isPhone(number)) {
                    getLoginCodeFun(number);
                } else {
                    Common.sendError("请输入正确的手机号");
                }
            }
        });
        //登录
        this.login = (Button) this.view.findViewById(R.id.login);
        this.login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String number = phone.getText().toString();
                if (!Common.isPhone(number)) {
                    Common.sendError("请输入正确的手机号");
                    return;
                }
                String code = loginCode.getText().toString();
                if (code.length() != 6) {
                    Common.sendError("请输入正确的登录码");
                    return;
                }
                //登录
                loginFun(number, code);

            }
        });
        //软键盘
        this.keyBoard = new KeyBoard(this.view, Constants.KEY_BOARD);
        this.keyBoard.setKeyBoardListener(new KeyBoard.KeyBoardListener() {
            @Override
            public void delete() {
                try {
                    EditText editText = (EditText) view.findFocus();
                    int index = editText.getSelectionStart();
                    if (index > 0) {
                        editText.getText().delete(index - 1, index);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void input(String c) {
                try {
                    EditText editText = (EditText) view.findFocus();
//                    editText.append(c);
                    Common.insert(editText, c);     //在光标后插入文字

                    if (editText.getId() == R.id.phone && Common.isPhone(editText.getText().toString())) {
                        getLoginCode.setEnabled(true);
                        getLoginCode.setTextColor(Color.parseColor("#ffffff"));
                        getLoginCode.setBackgroundResource(R.drawable.pink_radius);
                    } else {
                        getLoginCode.setEnabled(false);
                        getLoginCode.setTextColor(Color.parseColor("#999999"));
                        getLoginCode.setBackgroundResource(R.drawable.disabled_button);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        this.phone.requestFocus();
    }

    /**
     * 获取登录验证码
     */
    private void getLoginCodeFun(String phone) {
        Common.startLoad();
        JSONObject getCodeJson = new JSONObject();
        try {
            getCodeJson.put("user_account", phone);
            Common.log.write("发送获取登录码：" + getCodeJson.toString());
            Common.put.println(Common.encryptByDES(Common.packageJsonData(Constants.GET_LOGIN_CODE_CLASS, Constants.GET_LOGIN_CODE_METHOD, getCodeJson).toString(), Constants.DES_KEY));
            Common.put.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取登录码成功回调
     */
    public static void getLoginCodeSuccess() {
        Common.sendError("获取成功");
        final int _countdown = Layout3Frame.countDown;
        //发送禁用按钮消息
        Message msg = new Message();
        msg.obj = "";
        msg.what = Constants.DISABLED_BUTTON_MESSAGE;
        Common.loginFrameHandler.sendMessage(msg);
        //开始倒计时
        Common.loginFrameHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Layout3Frame.countDown--;
                Message message = new Message();
                message.obj = "";
                if (Layout3Frame.countDown == 0) {
                    Layout3Frame.countDown = _countdown;
                    message.what = Constants.ABLED_DOWN_BUTTON_MESSAGE;
                } else {
                    message.what = Constants.COUNT_DOWN_BUTTON_MESSAGE;
                    Common.loginFrameHandler.postDelayed(this, 1000);
                }
                Common.loginFrameHandler.sendMessage(message);
            }
        }, 1000);
    }

    /**
     * 登录
     *
     * @param phone 电话
     * @param code  登录码
     */
    private void loginFun(String phone, String code) {
        Common.startLoad();

        Common.tcpSocket.setTcpListener(new TcpListener() {
            @Override
            public void receive(Object message) {

            }
        });
        JSONObject getCodeJson = new JSONObject();
        try {
            getCodeJson.put("user_account", phone);
            getCodeJson.put("password", code);
            getCodeJson.put("login_type", 1);
            Common.log.write("开始登录：" + getCodeJson.toString());
            Common.put.println(Common.encryptByDES(Common.packageJsonData(Constants.LOGIN_JSON_CLASS, Constants.LOGIN_JSON_METHOD, getCodeJson).toString(), Constants.DES_KEY));
            Common.put.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取登录码成功回调
     */
    public static void loginSuccess() {
        Message message = new Message();
        message.what = Constants.LOGIN_SUCCESS_MESSAGE;
        message.obj = "";
        Common.loginFrameHandler.sendMessage(message);
    }
}
