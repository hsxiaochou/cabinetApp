package com.ss.testserial.JNI;

import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.util.Log;

import com.ss.testserial.Common.Common;

import java.util.HashMap;

/**
 * Created by tangly on 16-10-20.
 */

public class DataMaker {
    private static final int MAX_DOOR_NUM = 32;

    public static int[] makeOpenDoorArray(int boardId, int doorId) {
        int[] array = new int[5];
        array[0] = 0x8a;
        array[1] = boardId;
        array[2] = doorId;
        array[3] = 0x11;
        array[4] = (array[0] ^ array[1] ^ array[2] ^ array[3]) & 0xff;
        return array;
    }

    public static int getOpenDoorRecvArraySize() {

        return 0;
    }


    public static int[] makeGetAllLookStatusArray(int boardId) {
        int[] array = new int[5];
        array[0] = 0x80;
        array[1] = boardId;
        array[2] = 0x00;
        array[3] = 0x33;
        array[4] = (array[0] ^ array[1] ^ array[2] ^ array[3]) & 0xff;
        return array;
    }

    public static int getAllLookStatusRecvArraySize() {
        Common.confirm_LockBoardVsersion();//2次判断LockBoardVsersion
        if (Common.LockBoardVsersion.equals("V3.0")) {
            return 7;
        } else {
            return 8;
        }
    }

    public static HashMap<String, Boolean> getRecvAllLookStatus(int boardId, int[] recv) {
        if (boardId != recv[1])
            return null;
        HashMap<String, Boolean> DoorStatus = new HashMap<String, Boolean>();
        boolean[] lookStatus;
        Common.confirm_LockBoardVsersion();//2次判断LockBoardVsersion
        if (Common.LockBoardVsersion.equals("V3.0")) {
            if (recv[5] == 0x55) {
                recv[5] = 0x33;
            }
            int check = (recv[0] ^ recv[1] ^ recv[2] ^ recv[3] ^ recv[4]) & 0xff;
            if (recv[0] != 0x80 || recv[5] != 0x33 || recv[6] != check)
                return null;
            lookStatus = new boolean[24];
            getDoorStatusFromArray3(recv, lookStatus);
        } else if (Common.LockBoardVsersion.equals("V5.0")) {
            if (recv[6] == 0x55) {
                recv[6] = 0x33;
            }
            int check = (recv[0] ^ recv[1] ^ recv[2] ^ recv[3] ^ recv[4] ^ recv[5] ^ recv[6]) & 0xff;
            if (recv[0] != 0x80 || recv[6] != 0x33 || recv[7] != check)
                return null;
            lookStatus = new boolean[32];
            getDoorStatusFromArray5(recv, lookStatus);
        } else {
            Common.sendError("请联系管理员配置设备");
            return DoorStatus;
        }
        String doorNameComm = "door";
        for (int i = 0; i < lookStatus.length; i++) {
            DoorStatus.put(doorNameComm + (i + 1), lookStatus[i]);
        }
        return DoorStatus;
    }


    public static int[] makeGetAllIrStatusArray(int boardId) {
        int[] array = new int[5];
        array[0] = 0x80;
        array[1] = boardId;
        array[2] = 0x00;
        array[3] = 0x22;
        array[4] = (array[0] ^ array[1] ^ array[2] ^ array[3]) & 0xff;
        return array;
    }

    public static int getAllIrStatusRecvArraySize() {
        return 8;
    }

    public static HashMap<String, Boolean> getRecvAllIrStatus(int boardId, int[] recv) {
        if (boardId != recv[1])
            return null;
        int check = (recv[0] ^ recv[1] ^ recv[2] ^ recv[3] ^ recv[4] ^ recv[5] ^ recv[6]) & 0xff;
        if (recv[0] != 0x80 || recv[6] != 0x22 || recv[7] != check)
            return null;
        HashMap<String, Boolean> IrStatus = new HashMap<String, Boolean>();
        boolean[] irStatus = new boolean[MAX_DOOR_NUM];
        getDoorStatusFromArray5(recv, irStatus);
        String doorNameComm = "door";
        for (int i = 0; i < irStatus.length; i++) {
            IrStatus.put(doorNameComm + (i + 1), irStatus[i]);
        }
        return IrStatus;
    }

    private static void getDoorStatusFromArray3(int[] array, boolean[] doorStatus) {
        for (int i = 0; i < 3; i++) {
            int state = (int) array[4 - i];
            int t = 1;
            for (int j = 0; j < 8; j++) {
                int doorIndex = i * 8 + j;
                if ((t & state) > 0) {
                    doorStatus[doorIndex] = false;
                } else {
                    doorStatus[doorIndex] = true;
                }
                t *= 2;
            }
        }
    }

    private static void getDoorStatusFromArray5(int[] array, boolean[] doorStatus) {
        for (int i = 0; i < 4; i++) {
            int state = array[5 - i];
            int t = 1;
            for (int j = 0; j < 8; j++) {
                int doorIndex = i * 8 + j;
                if ((t & state) > 0) {
                    doorStatus[doorIndex] = true;
                } else {
                    doorStatus[doorIndex] = false;
                }
                t *= 2;
            }
        }
    }
}
