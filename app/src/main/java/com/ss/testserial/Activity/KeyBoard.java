package com.ss.testserial.Activity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.ss.testserial.Common.Common;
import com.ss.testserial.Common.Constants;
import com.ss.testserial.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Listen on 2016/10/23.
 */
public class KeyBoard {
    private LinearLayout keyboard;
    private KeyBoardListener keyBoardListener;
    private EditText editText;
    private Button[] buttons;
    private JSONObject key;
    private int[] id;

    public KeyBoard(View view, int key_board) {
        if (key_board == Constants.KEY_BOARD) {
            this.keyboard = (LinearLayout) view.findViewById(R.id.keyboard);
        } else {
            this.keyboard = (LinearLayout) view.findViewById(R.id.keyboard_num);
        }

        this.initKey();
        for (int i = 0; i < this.id.length; i++) {
            this.buttons[i] = (Button) view.findViewById(this.id[i]);
            this.buttons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        String s = key.getString(String.valueOf(view.getId()));
                        int selectionStart = editText.getSelectionStart();
                        if (s.equals("delete")) {
                            Common.log.write("键盘删除");
                            keyBoardListener.delete();
                            return;
                        } else {
                            Common.log.write("输入：" + s);
                            keyBoardListener.input(s);
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Button bt = (Button) view;
                    bt.getText().toString();
                }
            });
        }
    }

    public void setView(EditText editText) {
        this.editText = editText;
    }

    public void setKeyBoardListener(KeyBoardListener keyBoardListener) {
        this.keyBoardListener = keyBoardListener;
    }

    private void initKey() {
        this.buttons = new Button[38];
        this.id = new int[]{R.id.keyboard0, R.id.keyboard1, R.id.keyboard2, R.id.keyboard3, R.id.keyboard4, R.id.keyboard5, R.id.keyboard6, R.id.keyboard7, R.id.keyboard8, R.id.keyboard9,
                R.id.keyboardQ, R.id.keyboardW, R.id.keyboardE, R.id.keyboardR, R.id.keyboardT, R.id.keyboardY, R.id.keyboardU, R.id.keyboardI, R.id.keyboardO, R.id.keyboardP,
                R.id.keyboardA, R.id.keyboardS, R.id.keyboardD, R.id.keyboardF, R.id.keyboardG, R.id.keyboardH, R.id.keyboardJ, R.id.keyboardK, R.id.keyboardL,
                R.id.keyboardZ, R.id.keyboardX, R.id.keyboardC, R.id.keyboardV, R.id.keyboardB, R.id.keyboardN, R.id.keyboardM, R.id.keyboard_delete};
        try {
            this.key = new JSONObject();
            this.key.put("" + id[0], "0");
            this.key.put("" + id[1], "1");
            this.key.put("" + id[2], "2");
            this.key.put("" + id[3], "3");
            this.key.put("" + id[4], "4");
            this.key.put("" + id[5], "5");
            this.key.put("" + id[6], "6");
            this.key.put("" + id[7], "7");
            this.key.put("" + id[8], "8");
            this.key.put("" + id[9], "9");
            this.key.put("" + id[10], "Q");
            this.key.put("" + id[11], "W");
            this.key.put("" + id[12], "E");
            this.key.put("" + id[13], "R");
            this.key.put("" + id[14], "T");
            this.key.put("" + id[15], "Y");
            this.key.put("" + id[16], "U");
            this.key.put("" + id[17], "I");
            this.key.put("" + id[18], "O");
            this.key.put("" + id[19], "P");
            this.key.put("" + id[20], "A");
            this.key.put("" + id[21], "S");
            this.key.put("" + id[22], "D");
            this.key.put("" + id[23], "F");
            this.key.put("" + id[24], "G");
            this.key.put("" + id[25], "H");
            this.key.put("" + id[26], "J");
            this.key.put("" + id[27], "K");
            this.key.put("" + id[28], "L");
            this.key.put("" + id[29], "Z");
            this.key.put("" + id[30], "X");
            this.key.put("" + id[31], "C");
            this.key.put("" + id[32], "V");
            this.key.put("" + id[33], "B");
            this.key.put("" + id[34], "N");
            this.key.put("" + id[35], "M");
            this.key.put("" + id[36], "delete");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public interface KeyBoardListener {
        public void delete();

        public void input(String c);
    }
}
