package com.ss.testserial.Common;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.nfc.Tag;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.ss.testserial.Activity.Config;
import com.ss.testserial.Activity.GetFrame;
import com.ss.testserial.Activity.Layout3Frame;
import com.ss.testserial.Activity.MainActivity;
import com.ss.testserial.Activity.MainFrame;
import com.ss.testserial.Activity.PutPackageFrame;
import com.ss.testserial.Activity.SendFrame;
import com.ss.testserial.JNI.DeviceInterface;
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
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
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
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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


    //弹窗
    public static Dialog commonDialog = null;
    public static Dialog loadDialog = null;
    //确认开柜弹窗
    public static Dialog reOpenDialog = null;

    /*板子相关*/
    public static Thread registerBoardThread = null;
    public static boolean registerBoardThreadRun = true;
    public static boolean getBoardThreadRun = true;
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
    public static FileLog log;
    public static String contact_phone = "";
    public static String address = "";
    public static MainFrame mainFrame = null;

    /*banner*/
    public static int banners[] = {R.drawable.banner1, R.drawable.banner2, R.drawable.banner3};
    public static int bannerIndex = 0;
    // 新增图片修改
    public static int banners1[] = {R.drawable.main_banner_1, R.drawable.main_banner_2, R.drawable.main_banner_3};

    /*倒计时*/
    public static int count_down = Constants.RETURN_MAIN_ACTIVITY_TIME;
    public static int reboot_count_down = Constants.REBOOT_COUNT_DOWN;

    //网络断开重连注册
    public static boolean IS_REGIST = false;
    private static HttpURLConnection conn;
    private static boolean b;

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
        Message msg = new Message();
        msg.what = Constants.START_LOAD_MESSAGE;
        msg.obj = "";
        Common.mainActivityHandler.sendMessage(msg);
    }

    public static void endLoad() {
        Message msg = new Message();
        msg.what = Constants.END_LOAD_MESSAGE;
        msg.obj = "";
        Common.mainActivityHandler.sendMessage(msg);
    }

    public static void sendError(String message) {
        Message msg = new Message();
        msg.what = Constants.COMMON_ERROR_MESSAGE;
        msg.obj = message;
        Common.mainActivityHandler.sendMessage(msg);
    }

    public static boolean isPhone(String phone) {
        String regExp = "^1[34578]\\d{9}$";
        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(phone);
        return m.find();
    }

    //回到主界面
    public static void backToMain() {
        Message msg = new Message();
        msg.what = Constants.BACK_TO_MAIN_MESSAGE;
        msg.obj = "";
        Common.mainActivityHandler.sendMessage(msg);
    }

    //再次开柜
    public static void openAgain(JSONObject jsonObject) {
        Message msg = new Message();
        msg.what = Constants.OPEN_AGAIN_MESSAGE;
        msg.obj = jsonObject;
        Common.mainActivityHandler.sendMessage(msg);
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
            Common.put.println(Common.encryptByDES(Common.packageJsonData(Constants.GET_GRID_TYPE_CLASS, Constants.GET_GRID_TYPE_METHOD, new JSONObject()).toString(), Constants.DES_KEY));
            Common.put.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void reboot(Activity mAppContext) {
        Common.save(" 断网重启");
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
        mainActivity.getSharedPreferences(Constants.SP, 0).edit().putString(key, value).commit();

    }

    //获取preference的数据
    public static String getPreference(String key) {

        return mainActivity.getSharedPreferences(Constants.SP, 0).getString(key, "");
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
        Common.registerBoardThread = new Thread(new BoardInfo());
        Common.registerBoardThread.start();
    }


    //后台调节音量
    public static void getUpVolume(int num) {
        Log.e("TAG", "调节音量" + num);
        AudioManager mAudioManager = (AudioManager) Common.mainActivity.getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * num / 100, AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
    }


    //后台执行重启机器
    public static void rebot() {
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
    private static DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss",
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
        try {
            //找到sdcard上的文件
            File file = new File(path + "data.txt");
            if (file != null) {
                //仿Http协议发送数据方式进行拼接
                StringBuilder sb = new StringBuilder();
                sb.append("--" + BOUNDARY + "\r\n");
                sb.append("Content-Disposition: form-data; name=\"mac\"" + "\r\n");
                sb.append("\r\n");
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
                conn.setRequestProperty("HOST", Constants.LOG_HOST);
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
                Log.e("TAG", "连接状态：" + b);
            }
        } catch (Exception e) {
            Log.e("TAG", e.toString());
            e.printStackTrace();
        }
        return b;
    }
}
