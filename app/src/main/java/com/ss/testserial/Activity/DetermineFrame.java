package com.ss.testserial.Activity;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ss.testserial.Common.Common;
import com.ss.testserial.Common.Constants;
import com.ss.testserial.JNI.Jubu;
import com.ss.testserial.JNI.OpenGridListener;
import com.ss.testserial.R;

import org.json.JSONObject;

/**
 * Created by Administrator on 2018/2/23 0023.
 */

public class DetermineFrame extends Fragment {
    private LayoutInflater inflater;
    private View view = null;
    private Button bt_open_agin;
    private Button bt_ytd;
    private Button bt_wtd;

    private int boardId = 0;
    private int lockId = 0;
    private String frame = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        this.view = inflater.inflate(R.layout.determine_frame_view, container, false);
        Common.frame2 = "determine";
        this.init();
        return this.view;
    }


    private void init() {
        Common.determineFrameHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        if (getActivity() != null) {
                            if (Common.frame.equals("send")) {
                                getActivity().getFragmentManager().beginTransaction().replace(R.id.content, new SendFrame()).commitAllowingStateLoss();
                            } else {
                                getActivity().getFragmentManager().beginTransaction().replace(R.id.content, new PutPackageFrame()).commitAllowingStateLoss();
                                //开柜成功
                                Message mymsg = new Message();
                                msg.what = Constants.PUT_PACKAGE_SUCCESS_MESSAGE;
                                msg.obj = "";
                                Common.putFrameHandler.sendMessage(mymsg);
                            }
                        }
                        break;
                }
            }
        };


        bt_ytd = (Button) this.view.findViewById(R.id.bt_ytd);
        bt_ytd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.YTD();
                Common.frame2 = "";
            }
        });

        bt_wtd = (Button) this.view.findViewById(R.id.bt_wtd);
        bt_wtd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Common.frame2 = "";
                    JSONObject reply = new JSONObject();
                    reply.put("package_id", Common.package_id);
                    Common.put.println(Common.encryptByDES(Common.packageJsonData(Constants.GET_GRID_TYPE_CLASS, Constants.RESETLOCK, reply).toString(), Constants.DES_KEY));
                    Common.put.flush();
                    Common.startLoad();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        // 获取开柜数据
        JSONObject openData = Common.open_again_data;
        try {
            boardId = openData.getInt("boardId");
            lockId = openData.getInt("lockId");
            frame = openData.getString("frame");
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.bt_open_agin = (Button) this.view.findViewById(R.id.bt_open_agin);
        this.bt_open_agin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.save("再次开柜板子型号：" + Common.LockBoardVsersion + " boardId: " + boardId + " lockId " + lockId);//记录板子有关信息到文件中
                Common.confirm_LockBoardVsersion();//2次判断LockBoardVsersion
                // TODO:
                if (Common.LockBoardVsersion.equals(Constants.THIRD_BOX_NAME)) {
                    if (lockId == 22) {
                        lockId = 0;
                    }
                    Jubu.openBox(boardId, lockId);
                } else {
                    Common.device.openGrid(boardId, lockId, new OpenGridListener() {
                        @Override
                        public void openEnd() {
                            Common.sendError("开柜完成");
                        }
                    });
                }
            }
        });

    }
}
