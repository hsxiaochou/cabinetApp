package com.ss.testserial.Activity;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.ss.testserial.Common.Common;
import com.ss.testserial.Common.Constants;
import com.ss.testserial.JNI.Jubu;
import com.ss.testserial.JNI.OpenGridListener;
import com.smatek.uart.UartComm;
import com.ss.testserial.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by Listen on 2016/10/25.
 */
public class Config extends Fragment {
    private View view = null;
    private int boardId;
    private ArrayList<Button> test_buttons;
    private ArrayList<Button> buttons;
    private ArrayList<Button> lockButtons;
    private TextView config_mac;
    private JSONObject boardJson;
    private Button save_config;
    private Button exit;
    private Spinner lockboard_type;

    private UartComm UC;

    HandlerThread thread;
    private String uartDevice;
    private int baudrate;
    private int parityCheck;

    private boolean mRunning = false;
    private boolean mNeedSendData = false;
    private int[] mSendBuf = new int[256];
    private int mSendDataLen;
    private final int MSG_UPDATE = 1;
    private boolean mIsRS485 = false;
    private int uart_fd;
    private Handler mRecvHandler = null;
    private String recv_msg;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.frame_config, container, false);
        Common.frame = "config";
        this.init();
        return this.view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mRunning = false;

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
        Common.registerBoardThreadRun = false;
        Common.isBoardRegister = false;
        //初始化板地址按钮
        initBoard();
    }

    /**
     * 初始化板地址
     */
    private void initBoard() {
        //显示mac地址
        config_mac = (TextView) this.view.findViewById(R.id.config_mac);
        config_mac.setText(Common.mac);
        //初始化boardJson
        boardJson = new JSONObject();
        try {
            boardJson.put("" + R.id.save_board1, 0);
            boardJson.put("" + R.id.save_board2, 0);
            boardJson.put("" + R.id.save_board3, 0);
            boardJson.put("" + R.id.save_board4, 0);
            boardJson.put("" + R.id.save_board5, 0);
            boardJson.put("" + R.id.save_board6, 0);
        } catch (Exception e) {
        }
        //配置锁控板版本
        lockboard_type = (Spinner) this.view.findViewById(R.id.config_lockboard_type);
        try {
            switch (Common.LockBoardVsersion) {
                case "V5.0":
                    lockboard_type.setSelection(0);
                    break;
                case "V3.0":
                    lockboard_type.setSelection(1);
                    break;
                case Constants.THIRD_BOX_NAME:
                    lockboard_type.setSelection(2);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
        }
        //配置板按钮
        buttons = new ArrayList<Button>();
        buttons.add((Button) this.view.findViewById(R.id.save_board1));
        buttons.add((Button) this.view.findViewById(R.id.save_board2));
        buttons.add((Button) this.view.findViewById(R.id.save_board3));
        buttons.add((Button) this.view.findViewById(R.id.save_board4));
        buttons.add((Button) this.view.findViewById(R.id.save_board5));
        buttons.add((Button) this.view.findViewById(R.id.save_board6));
        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getBoard(view.getId());
                }
            });
        }
        for (int j = 0; j < Common.lockBoard.size(); j++) {
            switch (Common.lockBoard.get(j)) {
                case 1:
                    buttons.get(0).setTextColor(Color.parseColor("#ffffff"));
                    buttons.get(0).setBackgroundResource(R.drawable.primary_radius);
                    try {
                        boardJson.put("" + buttons.get(0).getId(), 1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    buttons.get(1).setTextColor(Color.parseColor("#ffffff"));
                    buttons.get(1).setBackgroundResource(R.drawable.primary_radius);
                    try {
                        boardJson.put("" + buttons.get(1).getId(), 2);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case 3:
                    buttons.get(2).setTextColor(Color.parseColor("#ffffff"));
                    buttons.get(2).setBackgroundResource(R.drawable.primary_radius);
                    try {
                        boardJson.put("" + buttons.get(2).getId(), 3);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case 4:
                    buttons.get(3).setTextColor(Color.parseColor("#ffffff"));
                    buttons.get(3).setBackgroundResource(R.drawable.primary_radius);
                    try {
                        boardJson.put("" + buttons.get(3).getId(), 4);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case 5:
                    buttons.get(4).setTextColor(Color.parseColor("#ffffff"));
                    buttons.get(4).setBackgroundResource(R.drawable.primary_radius);
                    try {
                        boardJson.put("" + buttons.get(4).getId(), 5);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case 6:
                    buttons.get(5).setTextColor(Color.parseColor("#ffffff"));
                    buttons.get(5).setBackgroundResource(R.drawable.primary_radius);
                    try {
                        boardJson.put("" + buttons.get(5).getId(), 6);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
        //配置锁按钮
        lockButtons = new ArrayList<Button>();
        lockButtons.add((Button) this.view.findViewById(R.id.config_lock1));
        lockButtons.add((Button) this.view.findViewById(R.id.config_lock2));
        lockButtons.add((Button) this.view.findViewById(R.id.config_lock3));
        lockButtons.add((Button) this.view.findViewById(R.id.config_lock4));
        lockButtons.add((Button) this.view.findViewById(R.id.config_lock5));
        lockButtons.add((Button) this.view.findViewById(R.id.config_lock6));
        lockButtons.add((Button) this.view.findViewById(R.id.config_lock7));
        lockButtons.add((Button) this.view.findViewById(R.id.config_lock8));
        lockButtons.add((Button) this.view.findViewById(R.id.config_lock9));
        lockButtons.add((Button) this.view.findViewById(R.id.config_lock10));
        lockButtons.add((Button) this.view.findViewById(R.id.config_lock11));
        lockButtons.add((Button) this.view.findViewById(R.id.config_lock12));
        lockButtons.add((Button) this.view.findViewById(R.id.config_lock13));
        lockButtons.add((Button) this.view.findViewById(R.id.config_lock14));
        lockButtons.add((Button) this.view.findViewById(R.id.config_lock15));
        lockButtons.add((Button) this.view.findViewById(R.id.config_lock16));
        lockButtons.add((Button) this.view.findViewById(R.id.config_lock17));
        lockButtons.add((Button) this.view.findViewById(R.id.config_lock18));
        lockButtons.add((Button) this.view.findViewById(R.id.config_lock19));
        lockButtons.add((Button) this.view.findViewById(R.id.config_lock20));
        lockButtons.add((Button) this.view.findViewById(R.id.config_lock21));
        lockButtons.add((Button) this.view.findViewById(R.id.config_lock22));
        lockButtons.add((Button) this.view.findViewById(R.id.config_lock23));
        lockButtons.add((Button) this.view.findViewById(R.id.config_lock24));
        for (int i = 0; i < lockButtons.size(); i++) {
            lockButtons.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openBox(view.getId());
                }
            });
        }
        //配置测试板按钮
        test_buttons = new ArrayList<Button>();
        test_buttons.add((Button) this.view.findViewById(R.id.test_board1));
        test_buttons.add((Button) this.view.findViewById(R.id.test_board2));
        test_buttons.add((Button) this.view.findViewById(R.id.test_board3));
        test_buttons.add((Button) this.view.findViewById(R.id.test_board4));
        test_buttons.add((Button) this.view.findViewById(R.id.test_board5));
        test_buttons.add((Button) this.view.findViewById(R.id.test_board6));
        for (int i = 0; i < test_buttons.size(); i++) {
            test_buttons.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for (int j = 0; j < test_buttons.size(); j++) {
                        if (view.getId() == test_buttons.get(j).getId()) {
                            test_buttons.get(j).setBackgroundResource(R.drawable.primary_radius);
                            test_buttons.get(j).setTextColor(Color.parseColor("#ffffff"));
                        } else {
                            test_buttons.get(j).setBackgroundResource(R.drawable.disabled_button);
                            test_buttons.get(j).setTextColor(Color.parseColor("#999999"));
                        }
                    }
                    switch (view.getId()) {
                        case R.id.test_board1:
                            boardId = 1;
                            break;
                        case R.id.test_board2:
                            boardId = 2;
                            break;
                        case R.id.test_board3:
                            boardId = 3;
                            break;
                        case R.id.test_board4:
                            boardId = 4;
                            break;
                        case R.id.test_board5:
                            boardId = 5;
                            break;
                        case R.id.test_board6:
                            boardId = 6;
                            break;
                        default:
                            break;
                    }
                }
            });
        }
        //退出程序按钮
        this.exit = (Button) this.view.findViewById(R.id.exit);
        this.exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.exit(0);
            }
        });
        //保存配置按钮
        this.save_config = (Button) this.view.findViewById(R.id.save_config);
        this.save_config.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Common.lockBoard = new ArrayList<Integer>();
                    JSONArray jsonArray = new JSONArray();
                    if (boardJson.getInt("" + R.id.save_board1) > 0) {
                        Common.lockBoard.add(boardJson.getInt("" + R.id.save_board1));
                        jsonArray.put(boardJson.getInt("" + R.id.save_board1));
                    }
                    if (boardJson.getInt("" + R.id.save_board2) > 0) {
                        Common.lockBoard.add(boardJson.getInt("" + R.id.save_board2));
                        jsonArray.put(boardJson.getInt("" + R.id.save_board2));
                    }
                    if (boardJson.getInt("" + R.id.save_board3) > 0) {
                        Common.lockBoard.add(boardJson.getInt("" + R.id.save_board3));
                        jsonArray.put(boardJson.getInt("" + R.id.save_board3));
                    }
                    if (boardJson.getInt("" + R.id.save_board4) > 0) {
                        Common.lockBoard.add(boardJson.getInt("" + R.id.save_board4));
                        jsonArray.put(boardJson.getInt("" + R.id.save_board4));
                    }
                    if (boardJson.getInt("" + R.id.save_board5) > 0) {
                        Common.lockBoard.add(boardJson.getInt("" + R.id.save_board5));
                        jsonArray.put(boardJson.getInt("" + R.id.save_board5));
                    }
                    if (boardJson.getInt("" + R.id.save_board6) > 0) {
                        Common.lockBoard.add(boardJson.getInt("" + R.id.save_board6));
                        jsonArray.put(boardJson.getInt("" + R.id.save_board6));
                    }
                    if (Common.lockBoard.size() > 0) {
                        //保存配置
                        Message message = new Message();
                        message.what = Constants.SAVE_CONFIG_MESSAGE;
                        JSONObject config_info = new JSONObject();
                        Common.LockBoardVsersion = lockboard_type.getSelectedItem().toString();
                        Common.savePreference("lock_board_version", Common.LockBoardVsersion);
                        config_info.put("lock_board_version", Common.LockBoardVsersion);
                        config_info.put("lock_board_array", jsonArray);
                        message.obj = config_info.toString();
                        Common.mainActivityHandler.sendMessage(message);
                        Common.log.write("保存配置：" + message.obj.toString());
                    } else {
                        Common.sendError("请选择锁控板");
                    }
                } catch (Exception e) {
                }
            }
        });
    }

    /**
     * 获取板信息
     *
     * @param buttonId
     */
    private void getBoard(int buttonId) {
        try {
            Log.d("TEST", String.valueOf(boardJson.getInt("" + buttonId)));
            int board_id = 0;
            switch (buttonId) {
                case R.id.save_board1:
                    board_id = 1;
                    break;
                case R.id.save_board2:
                    board_id = 2;
                    break;
                case R.id.save_board3:
                    board_id = 3;
                    break;
                case R.id.save_board4:
                    board_id = 4;
                    break;
                case R.id.save_board5:
                    board_id = 5;
                    break;
                case R.id.save_board6:
                    board_id = 6;
                    break;
                default:
                    break;
            }
            if (boardJson.getInt("" + buttonId) > 0) {
                boardJson.put("" + buttonId, 0);
                ((Button) this.view.findViewById(buttonId)).setBackgroundResource(R.drawable.disabled_button);
                ((Button) this.view.findViewById(buttonId)).setTextColor(Color.parseColor("#999999"));
            } else {
                boardJson.put("" + buttonId, board_id);
                ((Button) this.view.findViewById(buttonId)).setBackgroundResource(R.drawable.primary_radius);
                ((Button) this.view.findViewById(buttonId)).setTextColor(Color.parseColor("#ffffff"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openBox(int buttonId) {
        int lockId = 0;
        for (int j = 0; j < lockButtons.size(); j++) {
            if (buttonId == lockButtons.get(j).getId()) {
                lockButtons.get(j).setBackgroundResource(R.drawable.primary_radius);
                lockButtons.get(j).setTextColor(Color.parseColor("#ffffff"));
            } else {
                lockButtons.get(j).setBackgroundResource(R.drawable.disabled_button);
                lockButtons.get(j).setTextColor(Color.parseColor("#999999"));
            }
        }
        switch (buttonId) {
            case R.id.config_lock1:
                lockId = 1;
                break;
            case R.id.config_lock2:
                lockId = 2;
                break;
            case R.id.config_lock3:
                lockId = 3;
                break;
            case R.id.config_lock4:
                lockId = 4;
                break;
            case R.id.config_lock5:
                lockId = 5;
                break;
            case R.id.config_lock6:
                lockId = 6;
                break;
            case R.id.config_lock7:
                lockId = 7;
                break;
            case R.id.config_lock8:
                lockId = 8;
                break;
            case R.id.config_lock9:
                lockId = 9;
                break;
            case R.id.config_lock10:
                lockId = 10;
                break;
            case R.id.config_lock11:
                lockId = 11;
                break;
            case R.id.config_lock12:
                lockId = 12;
                break;
            case R.id.config_lock13:
                lockId = 13;
                break;
            case R.id.config_lock14:
                lockId = 14;
                break;
            case R.id.config_lock15:
                lockId = 15;
                break;
            case R.id.config_lock16:
                lockId = 16;
                break;
            case R.id.config_lock17:
                lockId = 17;
                break;
            case R.id.config_lock18:
                lockId = 18;
                break;
            case R.id.config_lock19:
                lockId = 19;
                break;
            case R.id.config_lock20:
                lockId = 20;
                break;
            case R.id.config_lock21:
                lockId = 21;
                break;
            case R.id.config_lock22:
                lockId = 22;
                break;
            case R.id.config_lock23:
                lockId = 23;
                break;
            case R.id.config_lock24:
                lockId = 24;
                break;
            default:
                break;
        }
        if (this.boardId > 0) {
            Log.d("开柜", "" + this.boardId + lockId);
            String lockVersion = lockboard_type.getSelectedItem().toString();
            // TODO:
            Common.save("板子型号：" + Common.LockBoardVsersion + " boardId: " + boardId + " lockId " + lockId);//记录板子有关信息到文件中
            if (lockVersion.equals(Constants.THIRD_BOX_NAME)) {
                if (lockId == 22) {
                    lockId = 0;
                }
                Jubu.openBox(this.boardId, lockId);
            } else {
                Common.device.openGrid(this.boardId, lockId, new OpenGridListener() {
                    @Override
                    public void openEnd() {
                    }
                });

//                Common.oPenDoor(boardId, lockId);
            }
        } else {
            Common.sendError("请选择锁控板");
        }
    }
}
