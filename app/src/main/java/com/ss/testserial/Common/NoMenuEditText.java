package com.ss.testserial.Common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

/**
 * Created by Administrator on 2017/12/15 0015.
 */


//禁止对EditText 做文件上的操作,包括弹出复制粘贴弹窗
public class NoMenuEditText extends EditText {
    private final Context context;

    boolean canPaste() {
        return false;
    }

    @Override
    public boolean isSuggestionsEnabled() {
        return false;
    }

    public NoMenuEditText(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public NoMenuEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public NoMenuEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        init();
    }

    private void init() {
        this.setCustomSelectionActionModeCallback(new ActionModeCallbackInterceptor());
        this.setLongClickable(false);
    }

    private class ActionModeCallbackInterceptor implements ActionMode.Callback {
        private final String TAG = NoMenuEditText.class.getSimpleName();

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }

        public void onDestroyActionMode(ActionMode mode) {
        }
    }
}