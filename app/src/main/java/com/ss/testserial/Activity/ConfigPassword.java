package com.ss.testserial.Activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ss.testserial.Common.Common;
import com.ss.testserial.Common.Constants;
import com.ss.testserial.R;

import java.lang.reflect.Field;

/**
 * Created by Listen on 2016/9/19.
 */
public class ConfigPassword extends Fragment{
    private View view = null;
    private ImageView back = null;
    private EditText[] code = null;
    private TextView contact_phone = null;
    private int position = 0;
    private KeyBoard keyBoard;
    private LayoutInflater inflater;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        this.view = inflater.inflate(R.layout.frame_config_password, container, false);
        Common.frame = "configPassword";
        this.init();
        return this.view;
    }
    @Override
    public void onDetach(){
        super.onDetach();
        try{
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this,null);
        }catch (NoSuchFieldException e1){
            throw new RuntimeException(e1);
        }catch (IllegalAccessException e2){
            throw new RuntimeException(e2);
        }
    }
    /**
     * 初始化
     */
    private void init(){
        this.contact_phone = (TextView)this.view.findViewById(R.id.contact_phone);
        this.contact_phone.setText(Common.contact_phone);
        //返回按钮
        this.back = (ImageView)this.view.findViewById(R.id.back2layout1);
        this.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //初始化
                keyBoard.setView(code[0]);
                position = 0;
                for (int i = 0;i<code.length;i++){
                    code[i].setText("");
                }
                FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
                MainFrame mainFrame = new MainFrame();
                fragmentTransaction.replace(R.id.content,mainFrame);
                fragmentTransaction.commitAllowingStateLoss();
            }
        });
        //验证码
        this.code = new EditText[6];
        this.code[0] = (EditText)this.view.findViewById(R.id.getCode1);
        this.code[1] = (EditText)this.view.findViewById(R.id.getCode2);
        this.code[2] = (EditText)this.view.findViewById(R.id.getCode3);
        this.code[3] = (EditText)this.view.findViewById(R.id.getCode4);
        this.code[4] = (EditText)this.view.findViewById(R.id.getCode5);
        this.code[5] = (EditText)this.view.findViewById(R.id.getCode6);
        //软键盘
        this.keyBoard = new KeyBoard(this.view, Constants.KEY_BOARD);
        this.keyBoard.setKeyBoardListener(new KeyBoard.KeyBoardListener() {
            @Override
            public void delete() {
                //出错清空
                if( position >= code.length ){
                    //初始化
                    keyBoard.setView(code[0]);
                    position = 0;
                    for (int i = 0;i<code.length;i++){
                        code[i].setText("");
                    }
                    return;
                }
                if( code[position].getText().length() == 0 && position > 0){
                    position--;
                    keyBoard.setView(code[position]);
                    code[position].setText("");
                }else{
                    code[position].setText("");
                }
            }
            @Override
            public void input(String c) {
                if(position<code.length){
                    code[position].setText(c);
                }
                position++;
                if( position<code.length ){
                    keyBoard.setView(code[position]);
                }
                if( position == code.length ){
                    getPackage();
                }
                //出错清空
                if( position > code.length ){
                    //初始化
                    keyBoard.setView(code[0]);
                    position = 0;
                    for (int i = 0;i<code.length;i++){
                        code[i].setText("");
                    }
                }
            }
        });
        this.keyBoard.setView(this.code[0]);
        for (int i = 0;i<this.code.length;i++) {
            this.code[i].setInputType(InputType.TYPE_NULL);
        }
    }

    /**
     * 发送取件信息
     */
    private void getPackage(){

        //获取验证码
        String code = "";
        for (int i = 0;i<this.code.length;i++){
            code += this.code[i].getText();
        }
        if(code.equals("171208")){
            //开始Frame事物
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            //添加Frame
            Config configFrame = new Config();
            fragmentTransaction.replace(R.id.content,configFrame);
            //提交事务
            fragmentTransaction.commitAllowingStateLoss();
        }else{
            Common.sendError("密码错误");
        }
    }
}
