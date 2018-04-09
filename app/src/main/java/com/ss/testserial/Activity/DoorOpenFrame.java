package com.ss.testserial.Activity;


import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ss.testserial.Common.Common;
import com.ss.testserial.Common.Constants;
import com.ss.testserial.JNI.Jubu;
import com.ss.testserial.R;

/**
 * Created by Administrator on 2018/1/30 0030.
 */

public class DoorOpenFrame extends Fragment  {
    private LayoutInflater inflater;
    private View view = null;
    private TextView tv_door_open;
    private Button ang_btn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        this.view = inflater.inflate(R.layout.door_open, container, false);
        Common.frame = "open_door";
        this.init();
        return this.view;
    }

    private void init() {
        ang_btn = (Button) this.view.findViewById(R.id.ang_btn);
        ang_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Common.LockBoardVsersion.equals(Constants.THIRD_BOX_NAME)) {
                    String check_boardId = Common.getPreference("check_boardId");
                    String check_lockId = Common.getPreference("check_lockId");
                    if (!TextUtils.isEmpty(check_boardId) && !TextUtils.isEmpty(check_lockId)) {
                        Toast.makeText(Common.mainActivity, "箱门状态监测中", Toast.LENGTH_SHORT).show();
//                        Jubu.getDoorStatus(Integer.parseInt(check_boardId), Integer.parseInt(check_lockId));
                    }
                }
            }
        });

    }
}
