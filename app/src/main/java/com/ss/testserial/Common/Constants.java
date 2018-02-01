package com.ss.testserial.Common;

import android.os.Environment;

import com.smatek.uart.UartComm;

/**
 * Created by Listen on 2016/9/9.
 */
public interface Constants {

    /*主板相关*/
    //获取设备失败间隔ms
    public static final int REGISTER_DEVICE_FAILED_DELAY = 100;
    //获取锁状态间隔ms
    public static final int GET_LOCK_STATE_DELAY = 1000 * 60;
    //主控板最大连接锁控板个数
    public static final int PER_MAX_LOCK_BOARD_COUNT = 4;
    //锁控板最大连接锁个数
    public static final int PER_MAX_LOCK_COUNT = 24;
    //获取所有锁状态时返回状态的数组长度
    public static final int LOCK_STATE_ARRAY_COUNT = 8;
    //获取单个锁状态时返回状态的数组长度
    public static final int PER_LOCK_STATE_ARRAY_COUNT = 5;
    //获取红外状态时返回状态的数组长度
    public static final int INFRARED_STATE_ARRAY_COUNT = 9;
    //红外板最大连接锁个数
    public static final int PER_MAX_INFRARED_COUNT = 24;
    // TODO:
    //三代柜版本名称
    public static final String THIRD_BOX_NAME = "jubu";


    /*TCP相关*/
    //服务器地址:端口
    public static final String HOST = "kdg.tuyingmjy.com";
//    public static final String HOST = "139.129.233.252";//测试
    //public static final String HOST = "106.75.146.181";
    public static final String DOMAIN = "http://kdg.tuyingmjy.com";
    //    public static final String DOMAIN = "139.129.233.252";//测试
    public static final int PORT = 8282;
    //日志上传host

    //视频下载url
    public static final String VIDEO_HOST = "http://" + HOST;
    public static final String LOG_URL = "http://" + HOST + "/index.php?m=Api&c=Api&a=uploadCabinetLog";


    //服务器重连间隔
    public static final int RECONNECT_DELAY = 10000;
    //连接最大阻塞时间
    public static final int CONNECT_BLOCK_TIMEOUT = 3 * 1000 * 60;
    //连接重发次数
    public static final int RE_SEND_COUNT = 60000;
    //连接重发间隔时间
    public static final int RE_SEND_TIME = 60000;


    /*消息类型*/
    //加载消息
    public static final int START_LOAD_MESSAGE = 0x01;
    public static final int END_LOAD_MESSAGE = 0x02;
    //取件消息
    public static final int GET_PACKAGE_SUCCESS_MESSAGE = 0x03;
    public static final int GET_PACKAGE_ERROR_MESSAGE = 0x04;
    //预约开柜消息
    public static final int CODE_PUT_PACKAGE_SUCCESS_MESSAGE = 0x05;
    public static final int CODE_PUT_PACKAGE_ERROR_MESSAGE = 0x06;
    //获取验证码消息
    public static final int DISABLED_BUTTON_MESSAGE = 0x07;
    public static final int COUNT_DOWN_BUTTON_MESSAGE = 0x08;
    public static final int ABLED_DOWN_BUTTON_MESSAGE = 0x09;
    //获取柜子返回消息
    public static final int GET_GRID_LIST_MESSAGE = 0x0A;
    //快递员投件开柜成功返回消息
    public static final int PUT_PACKAGE_SUCCESS_MESSAGE = 0x0B;
    //全局错误
    public static final int COMMON_ERROR_MESSAGE = 0x0C;
    //保存配置消息
    public static final int SAVE_CONFIG_MESSAGE = 0x0D;
    //返回主界面消息
    public static final int BACK_TO_MAIN_MESSAGE = 0x0E;
    //快递员登录成功消息
    public static final int LOGIN_SUCCESS_MESSAGE = 0x0F;
    //升级成功消息
    public static final int APK_UPDATE_SUCCESS_MESSAGE = 0x10;
    //未操作回到主界面
    public static final int RETURN_MAIN_ACTIVITY_MESSAGE = 0x11;
    //再次开柜
    public static final int OPEN_AGAIN_MESSAGE = 0x12;
    //再次开柜倒计时
    public static final int OPEN_AGAIN_COUNT_MESSAGE = 0x13;
    //注册成功
    public static final int REGISTER_SUCCESS_MESSAGE = 0x14;
    //倒计时消息
    public static final int COUNT_DOWN_MESSAGE = 0x15;
    //切换banner消息
    public static final int SWITCH_BANNER_MESSAGE = 0x16;
    //首页获取柜子剩余信息消息
    public static final int HOME_GET_GRID_LIST_MESSAGE = 0x17;       // 新增
    //查询获取成功消息
    public static final int QUERY_INFO = 0x18;
    //一键查询消息返回
    public static final int QUERY_SEND_INFO = 0x19;


    /*和服务器端交互JSON*/
    //注册设备类名
    public static final String REGISTER_DEVICE_JSON_CLASS = "Dev";
    //注册设备方法名
    public static final String REGISTER_DEVICE_JSON_METHOD = "register";
    //获取锁状态类名
    public static final String LOCK_STATUS_JSON_CLASS = "Dev";
    //获取锁状态方法名
    public static final String LOCK_STATUS_JSON_METHOD = "reportStatus";
    //取包裹类名
    public static final String GET_PACKAGE_JSON_CLASS = "DevOp";
    //取包裹方法名
    public static final String GET_PACKAGE_JSON_METHOD = "getPackageByCode";
    //快递员登录类名
    public static final String LOGIN_JSON_CLASS = "DevOp";
    //快递员登录方法名
    public static final String LOGIN_JSON_METHOD = "login";
    //预约投件类名
    public static final String CODE_SEND_PACKAGE_JSON_CLASS = "DevOp";
    //预约投件方法名
    public static final String CODE_SEND_PACKAGE_JSON_METHOD = "putPackageByCode";
    //快递员投件类名
    public static final String SEND_PACKAGE_JSON_CLASS = "DevOp";
    //快递员投件方法名
    public static final String SEND_PACKAGE_JSON_METHOD = "addPackage";
    //开柜类名
    public static final String OPEN_GRID_JSON_CLASS = "Dev";
    //开柜方法名
    public static final String OPEN_GRID_JSON_METHOD = "Open";
    //开柜回调类名
    public static final String OPEN_GRID_REPLY_JSON_CLASS = "Dev";
    //开柜回调方法名
    public static final String OPEN_GRID_REPLY_JSON_METHOD = "openReply";
    //获取登录码类名
    public static final String GET_LOGIN_CODE_CLASS = "DevOp";
    //获取登录码方法名
    public static final String GET_LOGIN_CODE_METHOD = "getLoginCode";
    //获取柜子类型类名
    public static final String GET_GRID_TYPE_CLASS = "DevOp";
    //获取柜子类型方法名
    public static final String GET_GRID_TYPE_METHOD = "getAvailableInfo";
    //设备扫码投件类名
    public static final String DEVICE_SCAN_CLASS = "DevOp";
    //设备扫码投件方法名
    public static final String DEVICE_SCAN_METHOD = "putPackageByScan";
    //升级操作类名
    public static final String UPDATE_CLASS = "Dev";
    //升级操作方法名
    public static final String UPDATE_METHOD = "update";
    //心跳包类名
    public static final String HEART_CLASS = "Dev";
    //心跳包方法名
    public static final String HEART_METHOD = "heartBeat";

    //增加调节音量
    public static final String VOLCLASS = "Dev";
    public static final String VOLMETHOD = "volumn";

    //查询信息
    public static final String QUERY_CLASS = "DevOp";
    public static final String QUERY_METHOD = "packageInfo";

    //一键获取验证码
    public static final String GETQUERY_CODE_CLASS = "DevOp";
    public static final String GETQUERY_CODE_METHOD = "resendMsg";
    //回收日志
    public static final String LOG_UP_CLASS = "Dev";
    public static final String LOG_UP_METHOD = "uploadLog";

    //定时清理日志
    public static final String CLEAR_LOG_CLASS = "Dev";
    public static final String CLEAR_LOG_METHOD = "clearLog";

    //定时开机
    public static final String REBOOT_CLASS = "Dev";
    public static final String REBOOT_METHOD = "reboot";

    //获取下载视频json
    public static final String GETVIDEO_CLASS = "DevOp";
    public static final String GETVIDEO_METHOD = "getAdVideo";


    /*其他*/
    //DES对称加密密钥
    public static final String DES_KEY = "ch2016sh";
    //SIGN签名密钥
    public static final String SIGN_KEY = "SmaRtHoMe2016zhihuijia0830";
    //设备操作前缀
    public static final String DEVICE_COMMAND_PREFIX = "device_command";
    //配置文件名
    public static final String SYSTEM_CONFIG = "system_config.txt";
    //崩溃日志名
    public static final String UNCAUGHT_EXCEPTION_LOG_FIEL = "yougoto_";
    //返回主界面操作延时
    public static final int RETURN_MAIN_ACTIVITY_TIME = 60;
    //切换banner时间
    public static final int SWITCH_BANNER_TIME = 10 * 1000;
    public static final int REBOOT_COUNT_DOWN = 18;
    public static final int LOAD_RUN_TIME_OUT = 15000; // 加载超时
    public static final int KEY_BOARD_NUM = 1; // 数字键盘
    public static final int KEY_BOARD = 2; // 英文键盘


    public static final String SP = "configuration";
    public static final String path = Environment.getExternalStorageDirectory()
            + "/crash/";

    int GET_VIDEO = 0x20;
    int DOWN_NEXT = 0X21;
    int DOOR_STATE = 0X22;
    int OPEN_DOOR = 0X23;
    int CLOSE_DOOR = 0X24;

}
