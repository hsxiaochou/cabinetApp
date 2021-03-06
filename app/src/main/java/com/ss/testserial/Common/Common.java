package com.ss.testserial.Common;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.nfc.Tag;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.smatek.uart.UartComm;
import com.ss.testserial.Activity.MainActivity;
import com.ss.testserial.Activity.MainFrame;
import com.ss.testserial.Activity.MyDialog;
import com.ss.testserial.JNI.DeviceInterface;
import com.ss.testserial.JNI.Jubu;
import com.ss.testserial.R;
import com.ss.testserial.Runnable.BoardInfo;
import com.ss.testserial.Runnable.TcpSocket;
import com.ss.testserial.Runnable.Update;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;


/**
 * Created by Listen on 2016/9/9.
 * 进程间通信
 */
public class Common {

    /*用户信息*/
    public static String wechat_id = "";
    public static String user_name = "";
    public static boolean overdue = false;
    //超时包裹支付二维码
    public static String pay_qrcode = "";
    //mac地址
    public static String mac = null;

    /*与服务器的长连接*/
    public static TcpSocket tcpSocket = null;
    //长连接
    public static Socket socket = null;
    //拉取信息流
    public static BufferedReader get = null;
    //上传信息流
    public static PrintWriter put = null;
    /*Thread*/
    public static Thread bannerThread = null;
    public static Thread countdownThread = null;
    public static Thread getBoxThread = null;
    public static Context context = null;
    public static String ISGETFRAGMENT = "";
    /*handler*/
    //更新主界面handler
    public static Handler mainActivityHandler = null;
    // TODO:
    public static Activity mainActivity = null;

    //更新收件handler
    public static Handler getFrameHandler = null;
    //更新收件handler
    public static Handler sendFrameHandler = null;
    //更新登录界面handler
    public static Handler loginFrameHandler = null;
    //快递员投件页面handler
    public static Handler putFrameHandler = null;
    //自助查询handler
    public static Handler queryFrameHandler = null;
    //快递员投递反馈handler
    public static Handler determineFrameHandler = null;

    //快递员回收handler
    public static Handler RecyclingFrameHandler = null;

    //弹窗
    public static Dialog commonDialog = null;

    /*板子相关*/
    public static Thread registerBoardThread = null;
    public static boolean registerBoardThreadRun = true;
    public static int boxid;
    //板子是否注册
    public static String LockBoardVsersion = "";
    public static boolean isBoardRegister = false;
    public static DeviceInterface device;

    /*配置项*/
    public static ArrayList<Integer> lockBoard = null;
    public static String version = null;
    public static Update update = null;

    /**/
    //再次开柜倒计时
    public static JSONObject open_again_data;
    public static String frame;
    public static String frame2 = "";
    public static FileLog log;
    public static String contact_phone = "";
    public static String address = "";
    public static MainFrame mainFrame = null;

    //开柜判断
    public static boolean isOpen = true;


    //快递员投递后的package_id
    public static String package_id = "";

    /*banner*/
    public static int banners[] = {R.drawable.banner1, R.drawable.banner2, R.drawable.banner3};
    public static int bannerIndex = 0;
    // 新增图片修改
    public static int banners1[] = {R.drawable.main_banner_1, R.drawable.main_banner_2, R.drawable.main_banner_3};

    /*倒计时*/
    public static int count_down = Constants.RETURN_MAIN_ACTIVITY_TIME;
    public static int reboot_count_down = Constants.REBOOT_COUNT_DOWN;

    //网络断开重连注册
    public static boolean IS_REGIST = false;//是否注册
    private static HttpURLConnection conn;
    private static boolean b;
    private static String content;


    public static String CrashLogName = "";
    public static UartComm.Rs485 rs485 = null;
    //获取门状态的判断
    public static int Door_status = -1;
    public static MyDialog myDialog = null;


    //判断是否有视频下载失败
    public static boolean HaveFailVideo = false;
    //判断视频是否下载完毕
    public static boolean isSuccessDown = false;

    //判断是否是用户寄件
    public static boolean type = false;
    public static Context applicationContext;

    /**
     * MD5加密
     *
     * @param buffer
     * @return
     */
    public static String getMd5(byte[] buffer) throws NoSuchAlgorithmException {
        String s = null;
        char hexDigist[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(buffer);
        byte[] datas = md.digest(); //16个字节的长整数
        char[] str = new char[2 * 16];
        int k = 0;
        for (int i = 0; i < 16; i++) {
            byte b = datas[i];
            str[k++] = hexDigist[b >>> 4 & 0xf];//高4位
            str[k++] = hexDigist[b & 0xf];//低4位
        }
        s = new String(str);
        return s;
    }

    /**
     * DES加密
     * Description DES加密
     *
     * @param data
     * @param key  加密键byte数组
     * @return
     * @throws Exception
     */
    public static String encryptByDES(String data, String key) throws Exception {
        // 生成一个可信任的随机数源
        SecureRandom sr = new SecureRandom();
        // 从原始密钥数据创建DESKeySpec对象
        DESKeySpec dks = new DESKeySpec(key.getBytes());
        // 创建一个密钥工厂，然后用它把DESKeySpec转换成SecretKey对象
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey securekey = keyFactory.generateSecret(dks);
        // Cipher对象实际完成加密操作
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        // 用密钥初始化Cipher对象
        cipher.init(Cipher.ENCRYPT_MODE, securekey, sr);
        return Base64.encodeToString(cipher.doFinal(data.getBytes()), Base64.DEFAULT).replace("\n", "");
    }

    /**
     * DES解密
     * Description DES解密
     *
     * @param data 密文
     * @param key  加密密钥
     * @return 解密后的字符串
     * @throws Exception
     */
    public static String decryptByDES(String data, String key) throws Exception {
        //data用base64decode
        byte[] baseData = Base64.decode(data, Base64.DEFAULT);
        // 生成一个可信任的随机数源
        SecureRandom sr = new SecureRandom();
        // 从原始密钥数据创建DESKeySpec对象
        DESKeySpec dks = new DESKeySpec(key.getBytes());
        // 创建一个密钥工厂，然后用它把DESKeySpec转换成SecretKey对象
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey securekey = keyFactory.generateSecret(dks);
        // Cipher对象实际完成解密操作
        Cipher cipher = Cipher.getInstance("DES");
        // 用密钥初始化Cipher对象，解密模式
        cipher.init(Cipher.DECRYPT_MODE, securekey, sr);
        return new String(cipher.doFinal(baseData));
    }

    /**
     * 将要发送的数据打包成JSON
     *
     * @param classString 请求操作的类名
     * @param method      请求操作的方法名
     * @param data        请求操作的数据
     * @return json对象
     */
    public static JSONObject packageJsonData(String classString, String method, Object data) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("class", classString);
            jsonObject.put("method", method);
            jsonObject.put("timestamp", String.valueOf(System.currentTimeMillis()));
            jsonObject.put("sign", Common.Md5FromJson(jsonObject));
            jsonObject.put("data", data);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return jsonObject;
    }

    /**
     * 对json签名存储到sign中
     *
     * @param json 参与计算的json
     * @return 加密后的字符串 失败返回空字符串
     */
    public static String Md5FromJson(JSONObject json) {
        try {
            //字符串按class=&method=&timestamp=排列
            String s = Constants.SIGN_KEY + "class=" + json.getString("class") + "&method=" + json.getString("method") + "&timestamp=" + json.getString("timestamp") + Constants.SIGN_KEY;
            //对字符串md5加密
            return Common.getMd5(s.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void startLoad() {
        Message msg = Common.mainActivityHandler.obtainMessage();
        msg.what = Constants.START_LOAD_MESSAGE;
        msg.obj = "";
        msg.sendToTarget();
    }

    public static void endLoad() {
        Message msg = Common.mainActivityHandler.obtainMessage();
        msg.what = Constants.END_LOAD_MESSAGE;
        msg.obj = "";
        msg.sendToTarget();
    }

    public static void sendError(String message) {
        Common.save(message);
        Message msg = Common.mainActivityHandler.obtainMessage();
        msg.what = Constants.COMMON_ERROR_MESSAGE;
        msg.obj = message;
        if (Common.mainActivityHandler != null) {
            msg.sendToTarget();
        }
    }

    public static boolean isPhone(String phone) {
        String regExp = "^1[3457869]\\d{9}$";
        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(phone);
        return m.find();
    }

    //回到主界面
    public static void backToMain() {
        Message msg = Common.mainActivityHandler.obtainMessage();
        msg.what = Constants.BACK_TO_MAIN_MESSAGE;
        msg.obj = "";
        msg.sendToTarget();
    }

    //再次开柜
    public static void openAgain(JSONObject jsonObject) {
        Message msg = Common.mainActivityHandler.obtainMessage();
        msg.what = Constants.OPEN_AGAIN_MESSAGE;
        msg.obj = jsonObject;
        msg.sendToTarget();
    }

    //快递员确定是否已投件
    public static void Determine(JSONObject jsonObject) {
        Message msg = Common.mainActivityHandler.obtainMessage();
        msg.what = Constants.DETERMINE;
        msg.obj = jsonObject;
        msg.sendToTarget();
    }


    //快递员已投件方法
    public static void YTD() {
        try {
            Common.startLoad();
            JSONObject reply = new JSONObject();
            reply.put("package_id", Common.package_id);
            if (Common.type) {
                Common.put.println(Common.encryptByDES(Common.packageJsonData(Constants.COMFIRM_SENd_class, Constants.COMFIRM_SENd_method, reply).toString(), Constants.DES_KEY));
                Common.put.flush();
                Common.type = false;
            } else {
                Common.put.println(Common.encryptByDES(Common.packageJsonData(Constants.GET_GRID_TYPE_CLASS, Constants.SENDMSG, reply).toString(), Constants.DES_KEY));
                Common.put.flush();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    //判断锁状态是否改变--判断状态数组是否都为空
    public static boolean isLockStatusChange(JSONArray lockStatusGroup) {
        try {
            for (int i = 0; i < lockStatusGroup.length(); i++) {
                if (lockStatusGroup.getJSONObject(i).getJSONArray("locks").length() != 0) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 获取剩余柜子数
    public static void getCabinetLeft() {
        try {
            if (Common.socket != null) {
                if (Common.socket.isConnected() && !Common.socket.isClosed()) {
                    if (Common.put != null) {
                        Common.put.println(Common.encryptByDES(Common.packageJsonData(Constants.GET_GRID_TYPE_CLASS, Constants.GET_GRID_TYPE_METHOD, new JSONObject()).toString(), Constants.DES_KEY));
                        Common.put.flush();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void reboot(Activity mAppContext) {
        Common.save(" 重启软件");
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


    //禁止软键盘弹出，但光标正常显示
    public static void disableShowSoftInput(EditText editText) {
        if (android.os.Build.VERSION.SDK_INT <= 10) {
            editText.setInputType(InputType.TYPE_NULL);
        } else {
            Class<EditText> cls = EditText.class;
            Method method;
            try {
                method = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
                method.setAccessible(true);
                method.invoke(editText, false);
            } catch (Exception e) {
            }

            try {
                method = cls.getMethod("setSoftInputShownOnFocus", boolean.class);
                method.setAccessible(true);
                method.invoke(editText, false);
            } catch (Exception e) {
            }
        }
    }


    //数据的preference存储
    public static void savePreference(String key, String value) {
        context.getSharedPreferences(Constants.SP, 0).edit().putString(key, value).commit();

    }

    //获取preference的数据
    public static String getPreference(String key) {
        return context.getSharedPreferences(Constants.SP, 0).getString(key, "");
    }


    //在光标处插入文字
    public static void insert(EditText editText, String c) {
        int index = editText.getSelectionStart();
        Editable editable = editText.getText();
        editable.insert(index, c);

    }

    public static void confirm_LockBoardVsersion() {
        if (!Common.LockBoardVsersion.equals(Common.getPreference("lock_board_version")) && !TextUtils.isEmpty(Common.getPreference("lock_board_version"))) {
            Common.LockBoardVsersion = getPreference("lock_board_version");
        } else {
            Common.savePreference("lock_board_version", Common.LockBoardVsersion);
        }
    }


    public static int JUBU_ZeroId(int lockid) {
        if (lockid == 22) {
            return 0;
        }
        return lockid;
    }


    //注册板子
    public static void register() {
//        if (Common.device == null) {
//            Common.sendError("没有主板");
//            return;
//        }
        Common.lockBoard = new ArrayList<Integer>();
        //读取配置文件
        File file = new File(Environment.getExternalStorageDirectory(), Constants.SYSTEM_CONFIG);
        FileInputStream in = null;
        ByteArrayOutputStream bout = null;
        byte[] buf = new byte[1024];
        bout = new ByteArrayOutputStream();
        int length = 0;
        byte[] content;
        JSONObject config_info;
        String lock_board_version;
        JSONArray lock_board_array;
        try {
            in = new FileInputStream(file);
            while ((length = in.read(buf)) != -1) {
                bout.write(buf, 0, length);
            }
            content = bout.toByteArray();
            config_info = new JSONObject(new String(content, "UTF-8"));
            lock_board_version = config_info.getString("lock_board_version");
            lock_board_array = config_info.getJSONArray("lock_board_array");
            try {
                Common.contact_phone = config_info.getString("phone");

            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Common.address = config_info.getString("address");
            } catch (Exception e) {
                e.printStackTrace();
            }
            Common.LockBoardVsersion = lock_board_version;
            for (int i = 0; i < lock_board_array.length(); i++) {
                Common.lockBoard.add(lock_board_array.getInt(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("MainFrame", "请配置锁控板");
            Common.sendError("请配置锁控板");
            return;
        }
        try {
            in.close();
            bout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Common.registerBoardThreadRun = true;
        Common.registerBoardThread.interrupt();
        Common.registerBoardThread = null;
        if (Common.registerBoardThread == null) {
            Common.registerBoardThread = new Thread(new BoardInfo());
            Common.registerBoardThread.start();
        }
    }


    //显示dialog

    public static void ShowDialog() {
        if (Common.myDialog == null) {
            Common.myDialog = new MyDialog(context);
        }
        Common.myDialog.show();

    }

    //设置Toast的显示时间
    public static void showMyToast(final Toast toast, final int cnt) {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                toast.show();
            }
        }, 0, 3000);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                toast.cancel();
                timer.cancel();
            }
        }, cnt);
    }

    //后台调节音量
    public static void getUpVolume(int num) {
        Log.e("TAG", "调节音量" + num);
        AudioManager mAudioManager = (AudioManager) Common.mainActivity.getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * num / 100, AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
    }


    //后台执行重启机器
    public static void rebot() {
        Common.save("重启机器！");
        Process proc = null;
        try {
            proc = Runtime.getRuntime().exec(new String[]{"su", "-c", "reboot "});
            proc.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 用于格式化日期,作为日志文件名的一部分
    private static DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd  HH-mm-ss",
            Locale.CHINA);


    //存储日志文件
    public static void save(String data) {
        try {
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                String time = formatter.format(new Date());
                data = time + ":     " + data + "\n";
                File dir = new File(Constants.path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(Constants.path + "data.txt", true);
                fos.write(data.toString().getBytes());
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //定时清理日志文件
    public static void ClearLog() {
        File file = new File(Constants.path + "data.txt");
        if (file != null) {
            if (file.isFile()) {
                file.delete();
            }
        }
    }


    //回收日志文件
    private static final String BOUNDARY = "---------------------------7db1c523809b2";//数据分割线

    public static boolean uploadHttpURLConnection(String path) {
        Log.e("TAG", path);
        try {
            //找到sdcard上的文件
            File file = new File(path);
            if (file != null) {
                //仿Http协议发送数据方式进行拼接
                StringBuilder sb = new StringBuilder();
                sb.append("--" + BOUNDARY + "\r\n");
                sb.append("Content-Disposition: form-data; name=\"mac\"" + "\r\n");
                sb.append("\r\n");
                Log.e("TAG", Common.getPreference("mac"));
                sb.append(Common.getPreference("mac") + "\r\n");
                sb.append("--" + BOUNDARY + "\r\n");
                sb.append("Content-Disposition: form-data; name=\"log\"; filename=\"" + "log1" + "\"" + "\r\n");
                sb.append("Content-Type: txt" + "\r\n");
                sb.append("\r\n");
                byte[] before = new byte[0];
                before = sb.toString().getBytes("UTF-8");
                byte[] after = ("\r\n--" + BOUNDARY + "--\r\n").getBytes("UTF-8");
                URL url = new URL(Constants.LOG_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
                conn.setRequestProperty("Content-Length", String.valueOf(before.length + file.length() + after.length));
                conn.setRequestProperty("HOST", Constants.HOST);
                conn.setDoOutput(true);

                OutputStream out = conn.getOutputStream();
                InputStream in = new FileInputStream(file);

                out.write(before);

                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) != -1)
                    out.write(buf, 0, len);

                out.write(after);
                in.close();
                out.close();
                b = conn.getResponseCode() == 200;
                if (200 == conn.getResponseCode()) {
                    InputStream inputStream = null;
                    if (!TextUtils.isEmpty(conn.getContentEncoding())) {
                        String encode = conn.getContentEncoding().toLowerCase();
                        if (!TextUtils.isEmpty(encode) && encode.indexOf("gzip") >= 0) {
                            inputStream = new GZIPInputStream(conn.getInputStream());
                        }
                    }
                    if (null == inputStream) {
                        inputStream = conn.getInputStream();
                    }

                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                    StringBuilder builder = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line).append("\n");
                    }
                    content = builder.toString();
                }
            }
        } catch (Exception e) {
            Log.e("TAG", e.toString());
            e.printStackTrace();
        }
        return b;
    }


    //遍历视频文件夹里的视频文件
    public static final List<File> mFileList = new ArrayList<>();

    public static List<File> getFile(File file) {
        File[] fileArray = file.listFiles();
        mFileList.clear();
        for (File f : fileArray) {
            if (f.isFile()) {
                if (f.getName().endsWith(".mp4")) {
                    mFileList.add(f);
                }
            } else {
                getFile(f);
            }
        }
        return mFileList;
    }

    public static final ArrayList<String> flienamelist = new ArrayList<>();

    //遍历文件夹里的文件 然后得到文件名字的集合
    public static List<String> getFileName(File myfile) {
        List<File> file = getFile(myfile);
        flienamelist.clear();
        for (int i = 0; i < file.size(); i++) {
            flienamelist.add(file.get(i).getName());
        }
        return flienamelist;
    }

    //获取视频下载json
    public static void GetVideoJson() {
        JSONObject getCodeJson = new JSONObject();
        try {
            getCodeJson.put("mac", Common.mac);
            Common.put.println(Common.encryptByDES(Common.packageJsonData(Constants.GETVIDEO_CLASS, Constants.GETVIDEO_METHOD, getCodeJson).toString(), Constants.DES_KEY));
            Common.put.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //获取视频下载json
    public static void GetImageOnNet() {
        JSONObject getCodeJson = new JSONObject();
        try {
            getCodeJson.put("mac", Common.mac);
            Common.put.println(Common.encryptByDES(Common.packageJsonData(Constants.GET_IMAGE_CLASS, Constants.GET_IMAGE_METHOD, getCodeJson).toString(), Constants.DES_KEY));
            Common.put.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void delateFile(final String path) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(path)) {
                    File file = new File(path);
                    if (file != null) {
                        file.delete();
                    }
                }
            }
        }).start();

    }


    public static void oPenDoor(final int boardId, final int lockId) {

        //再次开柜
        JSONObject grid_info = new JSONObject();
        try {
            grid_info.put("boardId", boardId);
            grid_info.put("lockId", lockId);
            Common.open_again_data=grid_info;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Timer timer = new Timer();
        if (rs485 == null) {
            rs485 = new UartComm().new Rs485();
            rs485.rs485Init();
        }
        final int[] ints = new int[5];
        rs485.rs485OpenGrid(boardId, lockId, ints);
        //只有取件才开2次柜
        if (Common.ISGETFRAGMENT.equals("get")) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    rs485.rs485OpenGrid(boardId, lockId, ints);
                }
            }, 100);
        }

    }

    /**
     * 隐藏虚拟按键，并且全屏
     */
    public static void hideBottomUIMenu() {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = Common.mainActivity.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = Common.mainActivity.getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    //jubu发送2次开柜
    public static void JuBuOpenAgain(final int boardId, final int lockId) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Jubu.openBox(boardId, lockId);
            }
        }, 500);
    }
}
