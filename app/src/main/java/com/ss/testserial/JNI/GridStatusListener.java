package com.ss.testserial.JNI;

import java.util.HashMap;

/**
 * Created by Listen on 2016/10/25.
 */
public interface GridStatusListener {
    public void getStatusEnd(int boardID, HashMap<String, Boolean> DoorStatus);
}
