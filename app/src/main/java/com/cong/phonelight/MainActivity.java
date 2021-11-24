package com.cong.phonelight;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.necer.ndialog.NDialog;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView iv, bjiv;
    private boolean mBackKeyPressed = false;//记录是否有首次按键
    private CameraManager mCameraManager;
    private String mCameraId;
    private boolean islight = true;

    private NotificationChannel channel;
    private NotificationManager manager;
    private MyBroadCast receiver;
    private int SUCCESS = 201;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 注册广播，并设置过滤条件
        receiver = new MyBroadCast();
        IntentFilter filter = new IntentFilter("a");
        registerReceiver(receiver, filter);

        initView();
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            mCameraId = mCameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        //开启闪光灯
        turnOnFlashLight();
    }

    class MyBroadCast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            bjiv.setImageResource(R.drawable.off);//关闭的图片
            turnOffFlashLight();
            manager.cancel(1);
            islight = false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initView() {
        iv = findViewById(R.id.iv);
        bjiv = findViewById(R.id.bjiv);
        iv.setOnClickListener(this);

        showNotify();
    }

    private void showNotify() {
        //通知用户开启通知
        NotificationManagerCompat notification = NotificationManagerCompat.from(this);
        boolean isEnabled = notification.areNotificationsEnabled();
        if (!isEnabled) {
            openNotification();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            // 通知渠道的id。
            String id = "1";
            // 用户可以看到的通知渠道的名字。
            CharSequence name = getResources().getString(R.string.app_name);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(id, name, importance);
            // 最后在notificationmanager中创建该通知渠道。
            manager.createNotificationChannel(mChannel);
            channel = manager.getNotificationChannel(id);
            if (channel.getImportance() == NotificationManager.IMPORTANCE_NONE) {
                openNotification();
            } else {
                //普通通知
                /*Notification notification = new NotificationCompat.Builder(this, id)
                        .setContentTitle("Test")//通知标题
                        .setContentText("通知内容")//通知内容
                        .setSmallIcon(R.mipmap.ic_launcher)//通知小图标
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))//通知大图标
                        .build();
                manager.notify(1, notification);*/

                RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.notification_small);
                notificationLayout.setTextViewText(R.id.notification_title, getResources().getString(R.string.app_name));
                Intent intent = new Intent("a");
                Intent intent2 = new Intent(this, MainActivity.class);
                notificationLayout.setOnClickPendingIntent(R.id.notification_btn, PendingIntent.getBroadcast(MainActivity.this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT));
                PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                PendingIntent pendingIntent2 = PendingIntent.getActivity(this, 1, intent2, PendingIntent.FLAG_UPDATE_CURRENT);

                Notification customNotification = new NotificationCompat.Builder(this, id)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                        .setCustomContentView(notificationLayout)//设置自定义View
                        .setDeleteIntent(pendingIntent)//设置侧滑删除通知
                        .setContentIntent(pendingIntent2)//设置可点击跳转
                        .build();
                manager.notify(1, customNotification);
            }
        }
    }

    private void openNotification() {
        new NDialog(this)
                .setTitle("提示")
                .setTitleColor(Color.parseColor("#000000"))
                .setTitleSize(18)
                .setTitleCenter(false)
                .setMessageCenter(false)
                .setMessage("打开通知更方便操作手电筒哦")
                .setMessageSize(16)
                .setMessageColor(Color.parseColor("#000000"))
                .setNegativeTextColor(Color.parseColor("#000000"))
                .setPositiveTextColor(Color.parseColor("#ff0000"))
                .setButtonCenter(false)
                .setButtonSize(14)
                .setCancleable(false)
                .setOnConfirmListener(which -> {
                    //which,0代表NegativeButton，1代表PositiveButton
                    if (which == 0) {

                    } else {
                        Intent intent = new Intent();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                            intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                intent.putExtra(Settings.EXTRA_CHANNEL_ID, 1);
                            }
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {  //5.0
                            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                            intent.putExtra("app_package", getPackageName());
                            intent.putExtra("app_uid", getApplicationInfo().uid);
                            startActivity(intent);
                        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {  //4.4
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.addCategory(Intent.CATEGORY_DEFAULT);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                        } else if (Build.VERSION.SDK_INT >= 15) {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                            intent.setData(Uri.fromParts("package", getPackageName(), null));
                        }
                        startActivityForResult(intent, SUCCESS);
                    }
                }).create(NDialog.CONFIRM).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SUCCESS) {
            showNotify();
        }
    }

    @Override
    public void onClick(View v) {
        try {
            if (islight) {
                bjiv.setImageResource(R.drawable.off);//关闭的图片
                turnOffFlashLight();
                islight = false;
                manager.cancel(1);
            } else {
                bjiv.setImageResource(R.drawable.on);//点亮的图片
                turnOnFlashLight();
                showNotify();
                islight = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 开启闪光灯的方法
     */
    private void turnOnFlashLight() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mCameraManager.setTorchMode(mCameraId, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭闪光的的方法
     */
    private void turnOffFlashLight() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mCameraManager.setTorchMode(mCameraId, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 双击返回键退出程序
     */
    public void onBackPressed() {
        if (!mBackKeyPressed) {
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            mBackKeyPressed = true;
            new Timer().schedule(new TimerTask() {//延时两秒，如果超出则清除第一次记录
                @Override
                public void run() {
                    mBackKeyPressed = false;
                }
            }, 2000);
        } else {//退出程序
            //关灯
            turnOffFlashLight();
            manager.cancel(1);
            islight = false;
            finish();
        }
    }
}

