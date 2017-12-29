package com.ss.testserial.JNI;

import android.util.Log;

import com.smatek.uart.UartComm;

/**
 * Created by tangly on 16-10-22.
 */

public class Scanner {
    private final String ScannerDevicePath = "/dev/ttyS2";
    private int mfd;
    private UartComm uartComm;
    private ReadThread readThread;

    public void open(){
        uartComm = new UartComm();
        mfd = uartComm.uartInit(ScannerDevicePath);
        Log.e("tangly", "mfd = " + mfd);
        Log.d("波特率","2");
        uartComm.setOpt(mfd, 9600, 8, 0, 1);
        this.readThread = new ReadThread();
        this.readThread.start();
    }

    public void close(){
        this.readThread.interrupt();
        uartComm.uartDestroy(mfd);
    }

    int addSize = 0;
    public void ScannerReader() {
        boolean dataIsStart = false;
        int[] recvData = new int[1000];
        long oldTime = 0;
        while (true) {
            addSize = 0;
            int[] buffTemp = new int[1000];
            while (true) {
                if (!dataIsStart)
                    oldTime = System.currentTimeMillis();
                int size = uartComm.recvWihtTimeOut(mfd, buffTemp, buffTemp.length, 100);
                if (size != 0 || dataIsStart) {
                    dataIsStart = true;
                    System.arraycopy(buffTemp, 0, recvData, addSize, size);
                    addSize += size;
                    if (System.currentTimeMillis() - oldTime > 300)
                        break;
                } else
                    continue;
            }

            byte[] temp = new byte[addSize];
            for (int i = 0; i < addSize; i++) {
                temp[i] = (byte)recvData[i];
            }
            String string = new String(temp).replace("\r\n","");
            Log.e("tangly", "addSize = " + addSize);
            Log.e("tangly", string);
            if (scannerInterface != null){
                scannerInterface.onScanerReaded(string);
            }
            addSize = 0;
            dataIsStart = false;
        }
    }
    class ReadThread extends Thread{
        @Override
        public void run() {
            super.run();
            try{
                ScannerReader();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    private ScannerInterface scannerInterface;
    public void setOnScanerReaded(ScannerInterface scannerInterface) {
        this.scannerInterface = scannerInterface;
    }
    public interface ScannerInterface {
        void onScanerReaded(String readData);
    }
}
