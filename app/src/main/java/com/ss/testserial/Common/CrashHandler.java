package com.ss.testserial.Common;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.ss.testserial.Activity.MainActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by leon on 2017/11/9.
 */

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    public static CrashHandler mAppCrashHandler;

    private Thread.UncaughtExceptionHandler mDefaultHandler;

    private MainActivity mAppContext;

    public static final String TAG = "TEST";
    // CrashHandler 实例
    private static CrashHandler INSTANCE = new CrashHandler();
    // 程序的 Context 对象

    // 用来存储设备信息和异常信息
    private Map<String, String> infos = new HashMap<String, String>();
    // 用来显示Toast中的信息
    private static String error = "";
    private static final Map<String, String> regexMap = new HashMap<String, String>();
    // 用于格式化日期,作为日志文件名的一部分
    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss",
            Locale.CHINA);


    public void initCrashHandler(MainActivity application) {
        this.mAppContext = application;
        // 获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public static CrashHandler getInstance() {
        if (mAppCrashHandler == null) {
            mAppCrashHandler = new CrashHandler();
        }
        return mAppCrashHandler;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            // 如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            AlarmManager mgr = (AlarmManager) mAppContext.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(mAppContext, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("crash", true);
            PendingIntent restartIntent = PendingIntent.getActivity(mAppContext, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent); // 1秒钟后重启应用

            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
            System.gc();
        }
    }

    /**
     * 错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        // 保存日志文件
        Common.CrashLogName = saveCrashInfoFile(ex);
        if (!TextUtils.isEmpty(Common.CrashLogName)) {
            new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            Common.uploadHttpURLConnection(Constants.path + Common.CrashLogName);
                        }
                    }
            ).start();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        // 自定义处理错误信息
        return true;
    }

    /**
     * 保存错误信息到文件中 *
     *
     * @param ex
     * @return 返回文件名称, 便于将文件传送到服务器
     */
    private String saveCrashInfoFile(Throwable ex) {
        StringBuffer sb = getTraceInfo(ex);
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
            long timestamp = System.currentTimeMillis();
            String time = formatter.format(new Date());
            String fileName = "carsh_error" + ".txt";
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                String path = Environment.getExternalStorageDirectory()
                        + "/crash/";
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(path + fileName);
                fos.write(sb.toString().getBytes());
                fos.close();
            }
            return fileName;
        } catch (Exception e) {
            Log.e(TAG, "an error occured while writing file...", e);
        }
        return null;
    }


    /**
     * 整理异常信息
     *
     * @param e
     * @return
     */
    public static StringBuffer getTraceInfo(Throwable e) {
        StringBuffer sb = new StringBuffer();
        Throwable ex = e.getCause() == null ? e : e.getCause();
        StackTraceElement[] stacks = ex.getStackTrace();
        for (int i = 0; i < stacks.length; i++) {
            if (i == 0) {
                setError(ex.toString());
            }
            sb.append("class: ").append(stacks[i].getClassName())
                    .append("; method: ").append(stacks[i].getMethodName())
                    .append("; line: ").append(stacks[i].getLineNumber())
                    .append("; Exception: ").append(ex.toString() + "\n");
        }
        return sb;
    }

    /**
     * 设置错误的提示语
     *
     * @param e
     */
    public static void setError(String e) {
        Pattern pattern;
        Matcher matcher;
        for (Map.Entry<String, String> m : regexMap.entrySet()) {
            pattern = Pattern.compile(m.getKey());
            matcher = pattern.matcher(e);
            if (matcher.matches()) {
                error = m.getValue();
                break;
            }
        }
    }


    /**
     * 初始化错误的提示语
     */
    private static void initMap() {
// Java.lang.NullPointerException
// java.lang.ClassNotFoundException
// java.lang.ArithmeticException
// java.lang.ArrayIndexOutOfBoundsException
// java.lang.IllegalArgumentException
// java.lang.IllegalAccessException
// SecturityException
// NumberFormatException
// OutOfMemoryError
// StackOverflowError
// RuntimeException
        regexMap.put(".*NullPointerException.*", "NullPointerException！");
        regexMap.put(".*ClassNotFoundException.*", "ClassNotFoundException！");
        regexMap.put(".*ArithmeticException.*", "ArithmeticException！");
        regexMap.put(".*ArrayIndexOutOfBoundsException.*", "ArrayIndexOutOfBoundsException！");
        regexMap.put(".*IllegalArgumentException.*", "IllegalArgumentException！");
        regexMap.put(".*IllegalAccessException.*", "IllegalAccessException！");
        regexMap.put(".*SecturityException.*", "SecturityException！");
        regexMap.put(".*NumberFormatException.*", "NumberFormatException！");
        regexMap.put(".*OutOfMemoryError.*", "OutOfMemoryError！");
        regexMap.put(".*StackOverflowError.*", "StackOverflowError！");
        regexMap.put(".*RuntimeException.*", "RuntimeException！");
    }
}