package com.ss.testserial.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.networkbench.agent.impl.NBSAppAgent;
import com.ss.testserial.Common.Common;
import com.ss.testserial.Common.Constants;
import com.ss.testserial.Common.CrashHandler;
import com.ss.testserial.Common.Loading;
import com.ss.testserial.Common.ToastUtil;
import com.ss.testserial.Common.download.DownloadService;
import com.ss.testserial.JNI.DeviceInterface;
import com.ss.testserial.R;
import com.ss.testserial.Runnable.TcpSocket;
import com.ss.testserial.Runnable.Update;
import com.ss.testserial.bean.GetImageBean;
import com.ss.testserial.bean.GetVideoUrl;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private ImageSwitcher bannerSwitch = null;
    private Loading load = null;
    // 新增图片修改
    private ImageSwitcher bannerSwitch1 = null;
    ExecutorService executorService = Executors.newFixedThreadPool(5);
    private DownloadService.DownloadBinder downloadBinder;
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder = (DownloadService.DownloadBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private String vediojson;
    private int index_video;
    private List<String> video_new;
    private List<String> fileName;
    int recLen = 0;
    Handler handler = new Handler();
    private GetVideoUrl getVideoUrl;
    private boolean isswitchx = false;
    private boolean door_sate_breack;
    private Banner banner;
    private String imagejson;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CrashHandler.getInstance().initCrashHandler(this);
        Common.mainActivity = this;
        //隐藏状态栏
        Common.hideBottomUIMenu();
        // Enable logging
        NBSAppAgent.setLicenseKey("f1122c57726f4d3ba066e525d981eeae").withLocationServiceEnabled(true).enableLogging(true).start(this.getApplicationContext());
        // Log last 100 messages
        NBSAppAgent.setLogging(100, "AndroidRuntime");
        Intent intent = new Intent(this, DownloadService.class);
        //这一点至关重要，因为启动服务可以保证DownloadService一直在后台运行，绑定服务则可以让MaiinActivity和DownloadService
        //进行通信，因此两个方法的调用都必不可少。
        startService(intent);  //启动服务
        bindService(intent, connection, BIND_AUTO_CREATE);//绑定服务
        //初始化
        init();
        //开启长连接线程                               // 修改
        if (Common.tcpSocket == null) {
            Common.tcpSocket = new TcpSocket();
            new Thread(Common.tcpSocket).start();
        }


        //设备操作
        Common.confirm_LockBoardVsersion();//确定LockBoardVsersion
        Log.e("TAG", "设备操作：" + Common.LockBoardVsersion + "   " + Common.device);
        if (!Common.LockBoardVsersion.equals(Constants.THIRD_BOX_NAME) && Common.device == null) {
            Common.device = new DeviceInterface();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);

        if (Common.mainActivityHandler != null) {
            Common.mainActivityHandler.removeCallbacksAndMessages(null);

        }
        if (Common.putFrameHandler != null) {
            Common.putFrameHandler.removeCallbacksAndMessages(null);

        }
        //更新收件handler
        if (Common.getFrameHandler != null) {
            Common.getFrameHandler.removeCallbacksAndMessages(null);

        }
        //更新收件handler
        if (Common.sendFrameHandler != null) {
            Common.sendFrameHandler.removeCallbacksAndMessages(null);

        }
        //更新登录界面handler
        if (Common.loginFrameHandler != null) {
            Common.loginFrameHandler.removeCallbacksAndMessages(null);

        }
        //快递员投件页面handler
        if (Common.putFrameHandler != null) {
            Common.putFrameHandler.removeCallbacksAndMessages(null);

        }

        //快递员回收handler
        if (Common.RecyclingFrameHandler != null) {
            Common.RecyclingFrameHandler.removeCallbacksAndMessages(null);

        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (isswitchx && Common.isSuccessDown) {
            timer.cancel();
            timer.start();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (Common.isSuccessDown) {
            timer.cancel();
            timer.start();
        }

        door_sate_breack = true;
        Common.count_down = Constants.RETURN_MAIN_ACTIVITY_TIME;
        return super.dispatchTouchEvent(ev);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            //开始Frame事物
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            //添加Frame
            ConfigPassword configFrame = new ConfigPassword();
            fragmentTransaction.replace(R.id.content, configFrame);
            //提交事务
            fragmentTransaction.commitAllowingStateLoss();
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    //初始化
    @SuppressLint("HandlerLeak")
    private void init() {
        Integer[] images = {R.drawable.banner1, R.drawable.banner2, R.drawable.banner3};
        List<Integer> integers = Arrays.asList(images);
        setBanner(integers);
        this.load = new Loading(this);
        //初始更新
        if (Common.update == null) {
            Common.update = new Update(this);
        }
        //获取软件版本
        if (Common.version == null) {
            PackageManager manager = getPackageManager();
            PackageInfo info = null;
            try {
                info = manager.getPackageInfo(getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            Common.version = info.versionName;
        }
        //初始化界面
        this.initView();
        //初始化播放视频倒计时
        // TODO Auto-generated method stub
        timer = new CountDownTimer(10 * 8000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // TODO Auto-generated method stub
                Log.e("TAG", millisUntilFinished / 1000 + "秒");
            }

            @Override
            public void onFinish() {
                Log.e("TAG","这是onfinish方法");
                startActivity(new Intent(MainActivity.this, VideoActivity.class));
                overridePendingTransition(R.anim.activity_right_in, R.anim.activity_left_out);
            }
        };


        //初始化handler
        if (Common.mainActivityHandler == null) {
            Common.mainActivityHandler = new Handler() {
                @Override
                public void handleMessage(final Message msg) {
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    Common.mainFrame = new MainFrame();
                    Window window = null;
                    WindowManager.LayoutParams lp = null;
                    switch (msg.what) {
                        case Constants.START_LOAD_MESSAGE:
                            load.show();
                            break;
                        case Constants.END_LOAD_MESSAGE:
                            load.hide();
                            break;
                        case Constants.COMMON_ERROR_MESSAGE:
                            ToastUtil toastUtil = new ToastUtil();
                            Toast show = toastUtil.Long(MainActivity.this, msg.obj.toString()).setPostion(685, 50).setToastBackground(Color.WHITE, R.drawable.toast_radius).show();
                            Common.showMyToast(show, 5000);
                            break;
                        //保存配置文件
                        case Constants.SAVE_CONFIG_MESSAGE:
                            final File file = new File(Environment.getExternalStorageDirectory(), Constants.SYSTEM_CONFIG);
                            FileOutputStream out = null;
                            try {
                                out = new FileOutputStream(file);
                                out.write(msg.obj.toString().getBytes("UTF-8"));
                                out.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            fragmentTransaction.replace(R.id.content, Common.mainFrame);
                            fragmentTransaction.commitAllowingStateLoss();
                            break;
                        case Constants.BACK_TO_MAIN_MESSAGE:
                            Common.wechat_id = "";
                            Common.user_name = "";
                            Common.pay_qrcode = "";
                            Common.frame = "main";
                            try {
                                Common.commonDialog.dismiss();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            load.hide();
                            /*
                        try {
                                Common.loadDialog.dismiss();
                            } catch (Exception e) {}
                            */
                            try {
                                Common.device.scanner.close();
                                fragmentTransaction.replace(R.id.content, Common.mainFrame);
                                fragmentTransaction.commitAllowingStateLoss();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case Constants.OPEN_AGAIN_MESSAGE:
                            // 开柜信息
                            JSONObject jsonObject;
                            Common.open_again_data = null;
                            try {
                                jsonObject = new JSONObject(msg.obj.toString());
                                jsonObject.put("frame", Common.frame);
                                Common.open_again_data = jsonObject;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            OpenDialogFrame openDialogFrame = new OpenDialogFrame();
                            fragmentTransaction.replace(R.id.content, openDialogFrame).commitAllowingStateLoss();
                            break;
                        case Constants.DETERMINE:
                            //快递员确定投递
                            Common.open_again_data = null;
                            try {
                                jsonObject = new JSONObject(msg.obj.toString());
                                jsonObject.put("frame", Common.frame);
                                Common.open_again_data = jsonObject;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            DetermineFrame determineFrame = new DetermineFrame();
                            fragmentTransaction.replace(R.id.content, determineFrame).commitAllowingStateLoss();

                            break;
                        case Constants.REGISTER_SUCCESS_MESSAGE:
                            try {
                                ((TextView) MainActivity.this.findViewById(R.id.contact_phone)).setText(Common.contact_phone);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                ((TextView) MainActivity.this.findViewById(R.id.address)).setText(Common.address);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Common.getCabinetLeft();
                            File file1 = new File(Environment.getExternalStorageDirectory(), Constants.SYSTEM_CONFIG);
                            FileInputStream in = null;
                            ByteArrayOutputStream bout = null;
                            FileOutputStream out1 = null;
                            byte[] buf = new byte[1024];
                            bout = new ByteArrayOutputStream();
                            int length = 0;
                            byte[] content;
                            JSONObject config_info;
                            String lock_board_version;
                            JSONArray lock_board_array;
                            try {
                                in = new FileInputStream(file1);
                                while ((length = in.read(buf)) != -1) {
                                    bout.write(buf, 0, length);
                                }
                                in.close();
                                bout.close();
                                content = bout.toByteArray();
                                config_info = new JSONObject(new String(content, "UTF-8"));
                                config_info.put("phone", Common.contact_phone);
                                config_info.put("address", Common.address);
                                out1 = new FileOutputStream(file1);
                                out1.write(config_info.toString().getBytes("UTF-8"));
                                out1.close();

                                //获取的信息为空。执行下载视频广告
                                if (TextUtils.isEmpty(vediojson)) {
                                    Common.GetVideoJson();
                                }

                                //获取网络图片
                                Common.GetImageOnNet();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case Constants.COUNT_DOWN_MESSAGE:
                            if (!TextUtils.isEmpty(Common.frame)) {
                                if (Common.frame == "main" || Common.frame == "config" || Common.frame == "open_door") {
                                    return;
                                }
                                Common.count_down--;
                                if (Common.count_down > 0) {
                                    //显示倒计时
                                    try {
                                        ((TextView) findViewById(R.id.count_down)).setText(Common.count_down + "s");
                                    } catch (Exception e) {
                                    }
                                } else {
                                    if (Common.frame.equals("determine")) {
                                        Common.YTD();
                                    }
                                    //初始化返回首页
                                    Common.count_down = Constants.RETURN_MAIN_ACTIVITY_TIME;
                                    Common.wechat_id = "";
                                    Common.user_name = "";
                                    Common.pay_qrcode = "";
                                    try {
                                        Common.commonDialog.dismiss();
                                    } catch (Exception e) {
                                    }
                                    load.hide();
                                    try {
                                        Common.device.scanner.close();
                                    } catch (Exception e) {
                                    }
                                    fragmentTransaction.replace(R.id.content,Common.mainFrame.instantiate(MainActivity.this, Common.mainFrame.getClass().getName()));
                                        fragmentTransaction.commitAllowingStateLoss();
                                    Common.frame2 = "";//还原
                                }
                            }

                            break;
                        case Constants.SWITCH_BANNER_MESSAGE:
                            if (Common.bannerIndex == Common.banners.length - 1) {
                                Common.bannerIndex = 0;
                            } else {
                                Common.bannerIndex++;
                            }
                            bannerSwitch.setImageResource(Common.banners[Common.bannerIndex]);
                            // 新增图片修改
                            bannerSwitch1.setImageResource(Common.banners1[Common.bannerIndex]);
                            if (Common.frame == "main") {
                                try {
                                    ((TextView) findViewById(R.id.date)).setText(new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));
                                    ((TextView) findViewById(R.id.week)).setText(new SimpleDateFormat("EEEE").format(new java.util.Date()));
                                } catch (Exception e) {
                                }
                            }
                            // 判断断网重启
                            if (--Common.reboot_count_down == 0) {
//                                Common.reboot(Common.mainActivity);
                                Common.rebot();//断网重启机器。
                            }
                            break;
                        case Constants.HOME_GET_GRID_LIST_MESSAGE:
                            if (Common.frame == "main") {
                                try {
                                    JSONArray jsonArray = (JSONArray) msg.obj;
                                    TextView[] cabinets = {
                                            (TextView) findViewById(R.id.cabinet_left1),
                                            (TextView) findViewById(R.id.cabinet_left2),
                                            (TextView) findViewById(R.id.cabinet_left3),
                                            (TextView) findViewById(R.id.cabinet_left4),
                                    };
                                    for (int i = 0; i < cabinets.length; i++) {
                                        String box_name = jsonArray.getJSONObject(i).getString("box_name");
                                        int box_num = jsonArray.getJSONObject(i).getInt("box_num");
                                        cabinets[i].setText(box_name + " " + box_num);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                        //下载视频json返回
                        case Constants.GET_VIDEO:
                            vediojson = (String) msg.obj;
                            getVideoUrl = new Gson().fromJson(vediojson, GetVideoUrl.class);
                            if (getVideoUrl.getData().isSwitchX() && getVideoUrl.getData().getVideo().size() > 0) {
                                isswitchx = true;
                                Log.e("TAG", "开始视频");
                                //新的videourl
                                video_new = new ArrayList<>();
                                for (int i = 0; i < getVideoUrl.getData().getVideo().size(); i++) {
                                    video_new.add(getVideoUrl.getData().getVideo().get(i).getName());
                                }
                                ArrayList<String> list_video = new ArrayList<>();
                                list_video.addAll(video_new);
                                fileName = Common.getFileName(new File(Constants.path));
                                int video_new_size = video_new.size();
                                for (int i = 0; i < video_new_size; i++) {
                                    String s = list_video.get(i);
                                    String substring = s.substring(s.lastIndexOf("/") + 1);
                                    if (fileName.contains(substring)) {
                                        fileName.remove(substring);
                                    }
                                }
                                for (int i = 0; i < fileName.size(); i++) {
                                    Log.e("TAG", "删除" + fileName.get(i));
                                    Common.delateFile(Constants.path +
                                            fileName.get(i));
                                }
                                index_video = 0;
                                Log.e("TAG", video_new.size() + "  ");
                                if (video_new.size() > 0) {
                                    startService(video_new.get(index_video));
                                } else {
                                    List<File> file_video = Common.getFile(new File(Constants.path));
                                    if (file_video.size() > 0) {
                                        timer.cancel();
                                        timer.start();
                                    }
                                }
                            } else {
                                isswitchx = false;
                            }
                            break;
                        case Constants.DOWN_NEXT:
                            index_video++;
                            if (index_video < video_new.size()) {
                                startService(video_new.get(index_video));
                            } else {
                                Common.isSuccessDown = true;
                                Common.HaveFailVideo = false;
                                timer.cancel();
                                timer.start();
                            }
                            break;
                        //JUBU柜子判断柜门的打开状态
                        case Constants.DOOR_STATE:
                            if (Common.Door_status == 0) {
                                if (Common.frame.equals("open_door")) {
                                    Toast.makeText(MainActivity.this, "感谢你关闭了箱门！", Toast.LENGTH_SHORT).show();
                                    fragmentTransaction.replace(R.id.content, Common.mainFrame);
                                    fragmentTransaction.commitAllowingStateLoss();
                                }
                            } else if (Common.Door_status == 1) {
                                Toast.makeText(MainActivity.this, "还有箱门未关闭，请关闭后操作！", Toast.LENGTH_SHORT).show();
                                if (!Common.frame.equals("open_door")) {
                                    //添加Frame
                                    DoorOpenFrame dooropenframe = new DoorOpenFrame();
                                    fragmentTransaction.replace(R.id.content, dooropenframe);
                                    //提交事务
                                    fragmentTransaction.commitAllowingStateLoss();
                                }
                            }
                            break;
                        case Constants.OPEN_DOOR:
                            //检查柜门的状态
                            String check_boardId = Common.getPreference("check_boardId");
                            String check_lockId = Common.getPreference("check_lockId");
                            if (!TextUtils.isEmpty(check_boardId) && !TextUtils.isEmpty(check_lockId)) {
                                Toast.makeText(MainActivity.this, "发送消息", Toast.LENGTH_SHORT).show();
//                                Jubu.getDoorStatus(Integer.parseInt(check_boardId), Integer.parseInt(check_lockId));
                            }
                            break;

                        case Constants.GET_IMAGE:
                            imagejson = (String) msg.obj;
                            Log.e("TAG", imagejson);
                            GetImageBean getImageBean = new Gson().fromJson(imagejson, GetImageBean.class);
                            List<String> big = getImageBean.getData().getList().getBig();
                            List<String> bigs = new ArrayList<>();
                            List<String> small = getImageBean.getData().getList().getSmall();

                            for (String item : big) {
                                bigs.add("http://" + Constants.HOST + item);
                            }
                            setBanner(bigs);
                            getPc(small);
                            break;
                        default:
                            break;
                    }
                }
            };
        }

    }

    private void getPc(List<String> list) {
        ArrayList<ImageView> imageview_pcs = new ArrayList<>();
        ImageView imageView_pc1 = findViewById(R.id.imageView_pc1);
        ImageView imageView_pc2 = findViewById(R.id.imageView_pc2);
        ImageView imageView_pc3 = findViewById(R.id.imageView_pc3);
        ImageView imageView_pc4 = findViewById(R.id.imageView_pc4);
        imageview_pcs.add(imageView_pc1);
        imageview_pcs.add(imageView_pc2);
        imageview_pcs.add(imageView_pc3);
        imageview_pcs.add(imageView_pc4);
        for (int i = 0; i < list.size(); i++) {
            Glide.with(Common.applicationContext).load("http://" + Constants.HOST + list.get(i)).placeholder(R.drawable.static2).error(R.drawable.static1).into(imageview_pcs.get(i));
        }


    }

    @Override
    protected void onStart() {
        super.onStart();
        banner.startAutoPlay();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //结束轮播
        banner.stopAutoPlay();
    }

    private void setBanner(List<?> list) {
        banner = (Banner) findViewById(R.id.eb_banner);
        //设置图片加载器
        banner.setImageLoader(new GlideImageLoader());
        //设置图片集合
        banner.setImages(list);
        //banner设置方法全部调用完毕时最后调用
        banner.setBannerStyle(BannerConfig.NOT_INDICATOR);
        banner.start();
    }


    private void startService(String s) {
        String url = Constants.VIDEO_HOST + s;
        downloadBinder.startDownload(url);
    }

    //初始化界面
    private void initView() {
        this.bannerSwitch = (ImageSwitcher) findViewById(R.id.bannerSwitch);
        this.bannerSwitch.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView imageView = new ImageView(getBaseContext());
                return imageView;
            }
        });
        this.bannerSwitch.setImageResource(Common.banners[Common.bannerIndex]);
        this.bannerSwitch.setInAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.slide_in_right));
        this.bannerSwitch.setOutAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.slide_out_left));
        // 新增图片修改
        this.bannerSwitch1 = (ImageSwitcher) findViewById(R.id.bannerSwitch1);
        this.bannerSwitch1.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView imageView = new ImageView(getBaseContext());
                return imageView;
            }
        });
        this.bannerSwitch1.setImageResource(Common.banners1[Common.bannerIndex]);
        this.bannerSwitch1.setInAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.slide_in_right));
        this.bannerSwitch1.setOutAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.slide_out_left));
        //初始化配置
        if (Common.lockBoard == null) {
            Common.lockBoard = new ArrayList<Integer>();
        }
        //开始Frame事物
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        //添加Frame
        MainFrame mainFrame = new MainFrame();
        fragmentTransaction.replace(R.id.content, mainFrame);
        //提交事务
        fragmentTransaction.commitAllowingStateLoss();
    }



}