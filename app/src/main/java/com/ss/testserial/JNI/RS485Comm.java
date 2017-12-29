package com.ss.testserial.JNI;

import android.util.Log;

import com.smatek.uart.UartComm;
import com.ss.testserial.Common.Common;
import com.ss.testserial.Runnable.BoardInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by tangly on 16-10-19.
 * modify by tangly on 16-11-03.
 */

public class RS485Comm extends UartComm {
    private static final int OPEN_DOOR_CMD = 1;
    private static final int ALL_IR_STATUS_CMD = 2;
    private static final int ALL_LOOK_STATUS_CMD = 3;
    private static final String TAG = "RS485Comm";
    private static final int RECV_TIMEOUT = 200;
    private static final int RECV_MAX_TIME = 500;
    private static int commandIdIndex = 1;
    private final int MAX_FIFO = 20;
    private static ArrayList<String> devices = new ArrayList<String>();
    private static ArrayList<RS485Comm> RS485Comms = new ArrayList<RS485Comm>();

    private int mFd = -1, mBaud, mDataBits, mParity, mStopBits;
    private LinkedBlockingQueue<Command> blockingQueue;
    public RS485CommInterface commInterface;
    private boolean[][] Irstatus, LookStatus;


    private RS485Comm() {
    }

    private RS485Comm(String device, int baud, int dataBits, int parity, int stopBits) {
        this.mBaud = baud;
        this.mDataBits = dataBits;
        this.mParity = parity;
        this.mStopBits = stopBits;
        mFd = uartInit(device);
        setOpt(mFd, baud, dataBits, parity, stopBits);
        setRS485WriteRead(1);
        Log.e(TAG, "init device : fd = " + mFd + " baud = " + baud + " dataBits = " + dataBits
                + " parity = " + parity + " stopBits = " + stopBits);
        blockingQueue = new LinkedBlockingQueue<Command>(MAX_FIFO);
        new commandThreadOneByOne().start();
    }

    public static RS485Comm getinstance(String device, int baud, int dataBits, int parity, int stopBits) {
        for (int i = 0; i < devices.size(); i++) {
            if (device.equals(devices.get(i))) {
                Log.e(TAG, "device " + device + "is exit, return null");
                return null;
            }
        }
        RS485Comm rs485Comm = new RS485Comm(device, baud, dataBits, parity, stopBits);
        if (rs485Comm.mFd < 0)
            return null;
        devices.add(device);
        RS485Comms.add(rs485Comm);
        return rs485Comm;
    }

//    public void rs485GetBoardAddress(int addrnum, int maxAddr, int[] retInfo){
//
//        Command command = new Command() {
//            @Override
//            void execAfterRecv(int[] recvData) {
//
//            }
//        };
//        try {
//            blockingQueue.put(command);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }

    public int openGridCmd(int boardID, int doorID) {
        Command command = new Command() {
            @Override
            void execAfterRecv(int[] recvData) {
                recv = recvData;
                String temp = "openGridCmd recv : ";
                for (int i = 0; i < recv.length; i++) {
                    temp += Integer.toHexString(recv[i]) + " ";
                }
                Log.e(TAG, temp);
                if (recvData != null) {
                    Log.e("TAG", recvData + ":recvdata");
                    currCommand.execSucc = true;
                    if (commInterface != null)
                        commInterface.onOpenGridEnd(commandID, boardID, doorID);
                }
            }
        };
        makeCommand(boardID, doorID, command, OPEN_DOOR_CMD);
        try {
            blockingQueue.put(command);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return command.commandID;
    }

    public int updateDoorStatusCmd(int boardID) {
        Command command = new Command() {
            @Override
            void execAfterRecv(int[] recvData) {

                if (recvData == null) {
                    currCommand.execSucc = true;
                    if (commInterface != null) {
                        commInterface.onUpdateDoorStatusEnd(-commandID, boardID, null);
                    }
                    return;
                }
                recv = recvData;

                String temp = "updateDoorStatusCmd recv : ";
                for (int i = 0; i < recv.length; i++) {
                    temp += Integer.toHexString(recv[i]) + " ";
                }
                Log.e(TAG, temp);
                HashMap<String, Boolean> doorStatus = DataMaker.getRecvAllLookStatus(boardID, recv);
                if (doorStatus != null) {
                    currCommand.execSucc = true;
                    if (commInterface != null)
                        commInterface.onUpdateDoorStatusEnd(commandID, boardID, doorStatus);
                }
            }
        };
        makeCommand(boardID, 0, command, ALL_LOOK_STATUS_CMD);
        try {
            blockingQueue.put(command);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return command.commandID;
    }

    public int updateIrStatusCmd(int boardID) {
        Command command = new Command() {
            @Override
            void execAfterRecv(int[] recvData) {
                if (recvData == null) {
                    currCommand.execSucc = true;
                    if (commInterface != null)
                        commInterface.onUpdateIrStatusEnd(-commandID, boardID, null);
                    return;
                }
                recv = recvData;
                String temp = "updateIrStatusCmd recv : ";
                for (int i = 0; i < recv.length; i++) {
                    temp += Integer.toHexString(recv[i]) + " ";
                }
                Log.e(TAG, temp);
                HashMap<String, Boolean> doorStatus = DataMaker.getRecvAllIrStatus(boardID, recv);
                if (doorStatus != null) {
                    currCommand.execSucc = true;
                    if (commInterface != null)
                        commInterface.onUpdateIrStatusEnd(commandID, boardID, doorStatus);
                }

            }
        };
        makeCommand(boardID, 0, command, ALL_IR_STATUS_CMD);
        try {
            blockingQueue.put(command);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return command.commandID;
    }

    private void makeCommand(int boardID, int doorId, Command command, int type) {
        switch (type) {
            case OPEN_DOOR_CMD:
                command.cmd = DataMaker.makeOpenDoorArray(boardID, doorId);
                command.recvSize = DataMaker.getOpenDoorRecvArraySize();
                break;
            case ALL_IR_STATUS_CMD:
                command.cmd = DataMaker.makeGetAllIrStatusArray(boardID);
                command.recvSize = DataMaker.getAllIrStatusRecvArraySize();
                break;
            case ALL_LOOK_STATUS_CMD:
                command.cmd = DataMaker.makeGetAllLookStatusArray(boardID);
                command.recvSize = DataMaker.getAllLookStatusRecvArraySize();
                break;
        }
        command.boardID = boardID;
        command.commandID = commandIdIndex++;
        if (commandIdIndex >= 30000)
            commandIdIndex = 1;
    }

    private Command currCommand = null;

    private class commandThreadOneByOne extends Thread {
        private long oldTime, maxDelayTime = 0;

        @Override
        public void run() {
            super.run();
            int addSize;
            try {
                while (true) {
                    if (currCommand == null || currCommand.execSucc) {
                        currCommand = blockingQueue.take();
                        currCommand.execTimes = 0;
                    }
                    int recvData[] = new int[currCommand.recvSize * 2];
                    for (int i = 0; i < recvData.length; i++)
                        recvData[i] = 0;
                    setRS485WriteRead(0);
                    Log.e(TAG, "send size = " + currCommand.cmd.length + " cmdId = " + currCommand.commandID);
                    send(mFd, currCommand.cmd, currCommand.cmd.length);
                    String string = "send data : ";
                    for (int i = 0; i < currCommand.cmd.length; i++)
                        string += "0x" + Integer.toHexString(currCommand.cmd[i]) + "  ";
                    Log.e(TAG, string);
                    setRS485WriteRead(1);
                    addSize = 0;
                    Log.e(TAG, "want recv size = " + currCommand.recvSize);
                    if (currCommand.execSucc)
                        oldTime = System.currentTimeMillis();
                    while (true) {
                        int[] recvDataTemp = new int[currCommand.recvSize];
                        int size = recvWihtTimeOut(mFd, recvDataTemp, recvDataTemp.length, RECV_TIMEOUT);
                        System.arraycopy(recvDataTemp, 0, recvData, addSize, size);
                        addSize += size;
                        Log.e(TAG, "recv size = " + size + " addSize = " + addSize);
                        if (addSize >= currCommand.recvSize) {
                            break;
                        }
                        if (System.currentTimeMillis() - oldTime > RECV_MAX_TIME) {
                            currCommand.execSucc = false;
                            currCommand.execTimes++;
                            break;
                        }
                    }
                    if (System.currentTimeMillis() - oldTime > maxDelayTime)
                        maxDelayTime = System.currentTimeMillis() - oldTime;
                    Log.e(TAG, "maxDelayTime = " + maxDelayTime);
                    if (addSize != currCommand.recvSize) {
                        Log.e(TAG, "- - - - - - - - - - - - - -");
                        currCommand.execSucc = false;
                        currCommand.execTimes++;
                    } else {
                        currCommand.execAfterRecv(recvData);
                    }
                    if (currCommand.execTimes >= 5) {
                        currCommand.execAfterRecv(null);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    abstract private class Command {
        abstract void execAfterRecv(int[] recvData);

        int commandID, boardID, doorID;
        boolean execSucc = true;
        int[] cmd, recv;
        int recvSize;
        int execTimes = 0;
    }

    public void setCommInterface(RS485CommInterface commInterface) {
        this.commInterface = commInterface;
    }

    public interface RS485CommInterface {
        void onOpenGridEnd(int commandID, int boardID, int doorID);

        void onUpdateDoorStatusEnd(int commandID, int boardID, HashMap<String, Boolean> DoorStatus);

        void onUpdateIrStatusEnd(int commandID, int boardID, HashMap<String, Boolean> DoorStatus);
    }
}
