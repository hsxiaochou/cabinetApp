package com.ss.testserial.Activity;

/**
 * Created by Administrator on 2018/1/17 0017.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;

import com.ss.testserial.R;

/**
 * 自定义弹框
 *
 * @author xiebin
 */
public class MyDialog extends AlertDialog {
    Context mContext;

    public MyDialog(Context context) {
        super(context, R.style.MyDialog); // 自定义全屏style
        setOwnerActivity((Activity)context);
        this.mContext = context;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void show() {
        super.show();
        /**
         * 设置宽度全屏，要设置在show的后面
         */
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().getDecorView().setPadding(0, 0, 0, 0);
        getWindow().setAttributes(layoutParams);
    }
}
