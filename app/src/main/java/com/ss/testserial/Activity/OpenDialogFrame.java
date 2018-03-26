package com.ss.testserial.Activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ss.testserial.Common.Common;
import com.ss.testserial.Common.Constants;
import com.ss.testserial.Common.QRCode;
import com.ss.testserial.JNI.Jubu;
import com.ss.testserial.JNI.OpenGridListener;
import com.ss.testserial.R;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.security.AlgorithmParameterGenerator;

/**
 * Created by Listen on 2016/9/19.
 */
public class OpenDialogFrame extends Fragment {

    private LayoutInflater inflater;
    private View view = null;
    private Button open_success = null;
    private Button open_again = null;
    private int boardId = 0;
    private int lockId = 0;
    private String frame = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        this.view = inflater.inflate(R.layout.open_again_dialog, container, false);
        Common.frame = "open_dialog";
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
        // 获取开柜数据
        JSONObject openData = Common.open_again_data;
        try {
            boardId = openData.getInt("boardId");
            lockId = openData.getInt("lockId");
            frame = openData.getString("frame");
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 开柜成功
        this.open_success = (Button) this.view.findViewById(R.id.open_success);
        this.open_success.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.overdue = false;
                switch (frame) {
                    case "get":
                        getActivity().getFragmentManager().beginTransaction().replace(R.id.content, new GetFrame()).commitAllowingStateLoss();
                        break;
                    case "send":
                        getActivity().getFragmentManager().beginTransaction().replace(R.id.content, new SendFrame()).commitAllowingStateLoss();
                        break;
                    case "scaner":
                        getActivity().getFragmentManager().beginTransaction().replace(R.id.content, new ScanFrame()).commitAllowingStateLoss();
                        break;
                    case "login":
                        getActivity().getFragmentManager().beginTransaction().replace(R.id.content, new Layout3Frame()).commitAllowingStateLoss();
                        break;
                    case "put":
                        getActivity().getFragmentManager().beginTransaction().replace(R.id.content, new PutPackageFrame()).commitAllowingStateLoss();
                        break;
                    case "recycling":
                        getActivity().getFragmentManager().beginTransaction().replace(R.id.content, new Recycling()).commitAllowingStateLoss();
                        break;
                    default:
                        getActivity().getFragmentManager().beginTransaction().replace(R.id.content, new MainFrame()).commitAllowingStateLoss();
                        break;
                }

            }
        });
        // 再次开柜
        this.open_again = (Button) this.view.findViewById(R.id.open_again);
        this.open_again.setOnClickListener(new View.OnClickListener() {
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
//                    Common.device.openGrid(boardId, lockId, new OpenGridListener() {
//                        @Override
//                        public void openEnd() {
//                            Common.sendError("开柜完成");
//                        }
//                    });
                    Common.oPenDoor(boardId, lockId);
                }
            }
        });
    }
}
