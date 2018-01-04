package com.ss.testserial.Activity;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.ss.testserial.Common.Common;
import com.ss.testserial.Common.Constants;
import com.ss.testserial.Common.NoMenuEditText;
import com.ss.testserial.R;
import com.ss.testserial.bean.QueryInfoBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/1/3 0003.
 */

public class QueryFrame extends Fragment {

    private LayoutInflater inflater;
    private KeyBoard keyBoard = null;
    private View view;
    private ImageView back2layout1_query;
    private NoMenuEditText phone_query;
    private Button clear_all;
    private ListView lv_no_get_list;
    private Button get_query_info;
    private String code;
    private Button get_query_code;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        this.view = inflater.inflate(R.layout.frame_layout_query, container, false);
        Common.frame = "query";
        this.init();
        return this.view;
    }


    @SuppressLint("HandlerLeak")
    private void init() {
        Common.queryFrameHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case Constants.QUERY_INFO:
                        Log.e("TAG", (String) msg.obj);
                        String s = (String) msg.obj;
                        QueryInfoBean queryInfoBean = new Gson().fromJson(s, QueryInfoBean.class);
                        List<String> list = queryInfoBean.getData().getList();
                        if (list.size() > 0 && list != null) {
                            get_query_code.setVisibility(View.VISIBLE);
                            DealInfo(list);
                        } else {
                            Common.sendError("抱歉，暂时没有您的包裹，请核对物流！");
                        }
                        break;
                }
            }
        };


        //点击返回键
        this.back2layout1_query = (ImageView) this.view.findViewById(R.id.back2layout1_query);
        this.back2layout1_query.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
                fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                MainFrame mainFrame = new MainFrame();
                fragmentTransaction.replace(R.id.content, mainFrame);
                fragmentTransaction.commitAllowingStateLoss();
            }
        });


        //电话号码或则运单号
        this.phone_query = (NoMenuEditText) this.view.findViewById(R.id.phone_query);

        Common.disableShowSoftInput(this.phone_query);
        this.phone_query.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                if (focus) {
                    phone_query.setTransformationMethod(null);
                    phone_query.setBackgroundResource(R.drawable.pink_border);
                    keyBoard.setView(phone_query);
                } else {
                    phone_query.setTransformationMethod(new PasswordTransformationMethod());
                    phone_query.setBackgroundResource(R.drawable.gray_border);
                }
            }
        });

        //点击清除按钮
        this.clear_all = (Button) this.view.findViewById(R.id.clear_all);
        clear_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View myview) {
                EditText editText = (EditText) view.findFocus();
                editText.setText("");
            }
        });


        //点击查询按钮
        this.get_query_info = (Button) this.view.findViewById(R.id.get_query_info);
        this.get_query_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                code = phone_query.getText().toString();
                if (!TextUtils.isEmpty(code)) {
                    getInfo(code);
                }
            }
        });
        //获取代码按钮
        this.get_query_code = (Button) this.view.findViewById(R.id.get_query_code);
        this.get_query_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        //软键盘
        this.keyBoard = new KeyBoard(this.view, Constants.KEY_BOARD_NUM);
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
                }
            }

            @Override
            public void input(String c) {
                try {
                    EditText editText = (EditText) view.findFocus();
                    Common.insert(editText, c);
                } catch (Exception e) {
                }
            }
        });

        this.phone_query.requestFocus();
        //listview 获取
        this.lv_no_get_list = (ListView) this.view.findViewById(R.id.lv_no_get_list);

    }

    /**
     * 发送查询信息
     *
     * @param code
     */
    private void getInfo(String code) {
        //获取验证码
        try {
            JSONObject data = new JSONObject();
            data.put("code_query", code);
            JSONObject jsonObject = Common.packageJsonData(Constants.QUERY_CLASS, Constants.QUERY_METHOD, data);
            if (Common.socket.isConnected() && !Common.socket.isClosed()) {
                Common.startLoad();
                Common.log.write("取件查询：" + this.code);
                Common.put.println(Common.encryptByDES(jsonObject.toString(), Constants.DES_KEY));
                Common.put.flush();
            } else {
                Common.sendError("网络连接失败，请稍后再试");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //处理查询返回信息
    public void DealInfo(List<String> list) {
        this.lv_no_get_list.setAdapter(new MyAdapter(getActivity(), list));
    }


    class MyAdapter extends BaseAdapter {
        private final List<String> list;
        LayoutInflater mInflater = null;

        public MyAdapter(Context context, List<String> list) {
            super();
            this.mInflater = LayoutInflater.from(context);
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.item_list_no_get_list, null);
                holder.ydh = (TextView) convertView.findViewById(R.id.ydh);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.ydh.setText(list.get(position));
            return convertView;
        }
    }

    public final class ViewHolder {
        public TextView ydh;
    }
}
