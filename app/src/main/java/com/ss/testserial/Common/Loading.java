package com.ss.testserial.Common;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.ss.testserial.Activity.MainActivity;
import com.ss.testserial.R;


/**
 * Created by leon on 2017/11/17.
 */

public class Loading extends AlertDialog.Builder {
    private final String TAG = "Loading";
    private Context context = null;
    private AlertDialog alertDialog = null;
    private Handler runtimeout = null;
    private LoadingListener loadingListener = null;

    public Loading(Context context) {
        super(context);
        this.context = context;
        super.setCancelable(false);
        this.runtimeout = new Handler();
    }

    public AlertDialog show() {
        if (this.alertDialog != null) {
            Log.d(this.TAG, "弹出层已存在");
            return this.alertDialog;
        }
        super.setView(View.inflate(this.context, R.layout.load, null)).create();
        this.alertDialog = super.show();
        Window window = this.alertDialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = 550;
        lp.gravity = Gravity.RIGHT;
        window.setAttributes(lp);

        this.runtimeout.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (alertDialog != null) {
                    loadFail();
                }
            }
        }, Constants.LOAD_RUN_TIME_OUT);
        return this.alertDialog;
    }

    public void hide() {
        try {
            if (alertDialog != null) {
                this.alertDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.alertDialog = null;
    }

    private void loadFail() {
        if (this.loadingListener == null) {
            Common.isOpen = true;
            Toast toast = Toast.makeText(this.context, "加载超时", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.BOTTOM, 685 + toast.getXOffset(), 50);
            toast.show();
        } else {
            this.loadingListener.fail();
        }
        this.hide();
    }

    public void setListener(LoadingListener loadingListener) {
        this.loadingListener = loadingListener;
    }

    public interface LoadingListener {
        public void fail();
    }
}
