package com.ss.testserial.Runnable;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import com.ss.testserial.Common.Common;

import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;

/**
 * Created by Listen on 2016/9/9.
 */
public class Wifi implements Runnable {
    private WifiManager wifiManager = null;
    private WifiInfo wifiInfo = null;

    //构造函数
    public Wifi(WifiManager wifiManager) {
        this.wifiManager = wifiManager;
    }

    // TODO: 下面全部修改
    @Override
    public void run() {
//        try {
//            String mac = null;
//            do{
//                this.tryOpenMAC();
//                this.wifiInfo = this.wifiManager.getConnectionInfo();
//                if( this.wifiInfo == null){
//                    continue;
//                }
//                mac = this.wifiInfo.getMacAddress();
//                Thread.sleep(100);
//            }while( mac == null );
//            Common.mac = mac.replace(":","").toUpperCase();
//            Common.log.write("获取到MAC地址："+Common.mac);
//        }catch (InterruptedException e) {
//            e.printStackTrace();
//            Common.log.write("获取MAC地址失败："+e.getMessage());
//        }
        try {
            String mac = null;
            do {
                mac = this.getMac();
                Thread.sleep(100);
            } while (mac == null);

            Common.mac = mac.replace(":", "").toUpperCase();
            if (TextUtils.isEmpty(Common.getPreference("mac"))) {
                Common.savePreference("mac", Common.mac);
            }
            if (!Common.mac.equals(Common.getPreference("mac"))) {
                Common.mac = Common.getPreference("mac");
            }

            Common.log.write("获取到MAC地址：" + Common.mac);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Common.log.write("获取MAC地址失败：" + e.getMessage());
        }

    }

    //尝试打开wifi
    private boolean tryOpenMAC() {
        if (!this.wifiManager.isWifiEnabled()) {
            this.wifiManager.setWifiEnabled(true);
        }
        return this.wifiManager.isWifiEnabled();
    }

    //尝试关闭MAC
    private boolean tryCloseMAC() {
        if (this.wifiManager.isWifiEnabled()) {
            this.wifiManager.setWifiEnabled(false);
        }
        return !this.wifiManager.isWifiEnabled();
    }

    /**
     * 获取手机的MAC地址
     *
     * @return
     */
    private String getMac() {
        String str = "";
        String macSerial = "";
        try {
            Process pp = Runtime.getRuntime().exec(
                    "cat /sys/class/net/wlan0/address ");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            for (; null != str; ) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();// 去空格
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (macSerial == null || "".equals(macSerial)) {
            try {
                return this.loadFileAsString("/sys/class/net/eth0/address")
                        .toUpperCase().substring(0, 17);
            } catch (Exception e) {
                e.printStackTrace();

            }

        }
        return macSerial;
    }

    private String loadFileAsString(String fileName) throws Exception {
        FileReader reader = new FileReader(fileName);
        String text = this.loadReaderAsString(reader);
        reader.close();
        return text;
    }

    private String loadReaderAsString(Reader reader) throws Exception {
        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[4096];
        int readLength = reader.read(buffer);
        while (readLength >= 0) {
            builder.append(buffer, 0, readLength);
            readLength = reader.read(buffer);
        }
        return builder.toString();
    }
}
