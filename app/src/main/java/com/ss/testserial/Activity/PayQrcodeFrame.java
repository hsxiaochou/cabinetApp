package com.ss.testserial.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ss.testserial.Common.Common;
import com.ss.testserial.Common.Constants;
import com.ss.testserial.Common.QRCode;
import com.ss.testserial.R;

import java.lang.reflect.Field;

/**
 * Created by Listen on 2016/9/19.
 */
public class PayQrcodeFrame extends Fragment {
    private LayoutInflater inflater;
    private View view = null;
    private ImageView back = null;
    private TextView pay_qrcode_text = null;
    private ImageView qrcode = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        this.view = inflater.inflate(R.layout.pay_qrcode, container, false);
        Common.frame = "pay_qrcode";
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
        //返回按钮
        this.back = (ImageView)this.view.findViewById(R.id.back2layout1);
        this.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //初始化
                Common.pay_qrcode = "";
                FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
                MainFrame mainFrame = new MainFrame();
                fragmentTransaction.replace(R.id.content,mainFrame);
                fragmentTransaction.commitAllowingStateLoss();
            }
        });
        //
        this.pay_qrcode_text = (TextView)this.view.findViewById(R.id.pay_qrcode_text);
        this.qrcode = (ImageView) this.view.findViewById(R.id.qrcode);


        if(Common.pay_qrcode.equals("")){
            this.pay_qrcode_text.setText("包裹存放超时，获取支付二维码失败，请重试");
        }else{
            this.pay_qrcode_text.setText("包裹存放超时，请使用微信扫码支付");
            this.qrcode.setImageBitmap(QRCode.makeQrcode(Common.pay_qrcode,250));
            ((ImageView)this.view.findViewById(R.id.qrcode2)).setImageBitmap(QRCode.makeQrcode(Common.pay_qrcode,190));
        }
    }
}
