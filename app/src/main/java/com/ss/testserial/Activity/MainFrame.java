package com.ss.testserial.Activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ss.testserial.Common.Common;
import com.ss.testserial.Common.Constants;
import com.ss.testserial.JNI.Jubu;
import com.ss.testserial.R;
import com.ss.testserial.Runnable.BoardInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by Listen on 2016/9/19.
 */
public class MainFrame extends Fragment {
    private ImageButton send = null;
    private ImageButton get = null;
    private ImageButton login = null;
    private View view = null;
    private TextView contact_phone = null;
    private TextView address = null;
    private TextView version = null;
    private TextView date = null;
    private TextView week = null;

    private ImageButton query;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.frame_layout1, container, false);
        this.init();
        Common.frame = "main";
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
    public void init() {
        this.contact_phone = (TextView) this.view.findViewById(R.id.contact_phone);
        this.address = (TextView) this.view.findViewById(R.id.address);
        this.version = (TextView) this.view.findViewById(R.id.version);
        try {
            this.version.setText("V" + this.getActivity().getPackageManager().getPackageInfo(this.getActivity().getPackageName(), 0).versionName.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.date = (TextView) this.view.findViewById(R.id.date);
        this.week = (TextView) this.view.findViewById(R.id.week);
        this.date.setText(new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));
        this.week.setText(new SimpleDateFormat("EEEE").format(new java.util.Date()));
        //开始注册板
        this.register();
        //寄件按钮
        this.send = (ImageButton) this.view.findViewById(R.id.layout1_send);
        this.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Common.lockBoard.size() > 0) {
                    FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
                    fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);//my加上动画
                    SendFrame sendFrame = new SendFrame();
                    fragmentTransaction.replace(R.id.content, sendFrame);
                    fragmentTransaction.commitAllowingStateLoss();
                } else {
                    Toast.makeText(view.getContext(), "没有连接任何柜子", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //取件按钮
        this.get = (ImageButton) this.view.findViewById(R.id.layout1_get);
        this.get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Common.lockBoard.size() > 0) {
                    FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
                    fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);//my加上动画
                    GetFrame getFrame = new GetFrame();
                    fragmentTransaction.replace(R.id.content, getFrame);
                    fragmentTransaction.commitAllowingStateLoss();
                } else {
                    Toast.makeText(view.getContext(), "没有连接任何柜子", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //登录按钮
        this.login = (ImageButton) this.view.findViewById(R.id.send2login);
        this.login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
                fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                Layout3Frame layout3Frame = new Layout3Frame();
                fragmentTransaction.replace(R.id.content, layout3Frame);
                Common.log.write("点击投件码登录界面登录按钮");
                fragmentTransaction.commitAllowingStateLoss();
            }
        });

        //查询按钮
        this.query = (ImageButton) this.view.findViewById(R.id.query);
        this.query.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
                fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                QueryFrame queryFrame = new QueryFrame();
                fragmentTransaction.replace(R.id.content, queryFrame);
                Common.log.write("点击查询按钮");
                fragmentTransaction.commitAllowingStateLoss();

            }
        });
        // 获取剩余柜子数
        Common.getCabinetLeft();
    }


    /**
     * 开始注册板信息
     */
    public void register() {
//        if (Common.device == null) {
//            Common.sendError("没有主板");
//            return;
//        }
        Common.lockBoard = new ArrayList<Integer>();
        //读取配置文件
        File file = new File(Environment.getExternalStorageDirectory(), Constants.SYSTEM_CONFIG);
        FileInputStream in = null;
        ByteArrayOutputStream bout = null;
        byte[] buf = new byte[1024];
        bout = new ByteArrayOutputStream();
        int length = 0;
        byte[] content;
        JSONObject config_info;
        String lock_board_version;
        JSONArray lock_board_array;
        try {
            in = new FileInputStream(file);
            while ((length = in.read(buf)) != -1) {
                bout.write(buf, 0, length);
            }
            content = bout.toByteArray();
            config_info = new JSONObject(new String(content, "UTF-8"));
            lock_board_version = config_info.getString("lock_board_version");
            lock_board_array = config_info.getJSONArray("lock_board_array");
            try {
                Common.contact_phone = config_info.getString("phone");
                this.contact_phone.setText(Common.contact_phone);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Common.address = config_info.getString("address");
                this.address.setText(Common.address);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Common.LockBoardVsersion = lock_board_version;
            for (int i = 0; i < lock_board_array.length(); i++) {
                Common.lockBoard.add(lock_board_array.getInt(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("MainFrame", "请配置锁控板");
            Common.sendError("请配置锁控板");
            return;
        }
        try {
            in.close();
            bout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Common.registerBoardThreadRun = true;
        if (Common.registerBoardThread == null) {
            Common.registerBoardThread = new Thread(new BoardInfo());
            Common.registerBoardThread.start();
        }
    }
}