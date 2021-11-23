package com.cong.phonelight;

import java.lang.reflect.Field;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.necer.ndialog.NDialog;

import static android.content.Intent.ACTION_DELETE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView iv, bjiv;
    private boolean mBackKeyPressed = false;//记录是否有首次按键
    private CameraManager mCameraManager;
    private String mCameraId;
    private boolean islight = false;

    private NotificationManager mNotificationManager;
    private String id;
    private NotificationChannel channel;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            mCameraId = mCameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        //开启闪光灯
        turnOnFlashLight();
        islight = true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initView() {
        iv = findViewById(R.id.iv);
        bjiv = findViewById(R.id.bjiv);
        iv.setOnClickListener(this);

        //通知用户开启通知
        NotificationManagerCompat notification = NotificationManagerCompat.from(this);
        boolean isEnabled = notification.areNotificationsEnabled();
        if (!isEnabled) {
            openNotification();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            channel = manager.getNotificationChannel("1");
            if (channel.getImportance() == NotificationManager.IMPORTANCE_NONE) {
                openNotification();
            }
        }

        notify_customview();
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
                        startActivity(intent);
                    }
                }).create(NDialog.CONFIRM).show();
    }

    private void notify_customview() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            // 通知渠道的id。
            id = "1";
            // 用户可以看到的通知渠道的名字。
            CharSequence name = getString(R.string.app_name);
            // 用户可以看到的通知渠道的描述。
            String description = getString(R.string.app_name);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(id, name, importance);
            // 配置通知渠道的属性。
            mChannel.setDescription(description);
            // 设置通知出现时的闪灯（如果Android设备支持的话）。
//            mChannel.enableLights(false);
//            mChannel.setLightColor(Color.RED);
            // 设置通知出现时的震动（如果Android设备支持的话）。
//            mChannel.enableVibration(true);
//            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            // 最后在notificationmanager中创建该通知渠道。
            mNotificationManager.createNotificationChannel(mChannel);
        }

        Intent intent = new Intent(this, MainActivity.class);
        int notifyid = (int) System.currentTimeMillis();
        PendingIntent pendingIntent = PendingIntent.getActivity(this, notifyid, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentCancel = new Intent(getApplicationContext(), NotificationBroadcastReceiver.class);
        PendingIntent pendingIntentCancel = PendingIntent.getBroadcast(getApplicationContext(), 0,
                intentCancel, PendingIntent.FLAG_ONE_SHOT);

        Notification notification = new NotificationCompat.Builder(this, id)
                .setContentTitle(getResources().getString(R.string.app_name))
//                .setContentText(getResources().getString(R.string.app_name))
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentIntent(pendingIntent)//设置可点击跳转
                .setDeleteIntent(pendingIntentCancel)//取消消息回调
                .build();
        mNotificationManager.notify(1, notification);
    }

    public class NotificationBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int type = intent.getIntExtra("type", -1);

            if (type != -1) {
                NotificationManager notificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(type);
            }

            turnOffFlashLight();
            islight = false;
        }
    }

    @Override
    public void onClick(View v) {
        try {
            if (islight) {
                bjiv.setImageResource(R.drawable.off);//关闭的图片
                turnOffFlashLight();
                mNotificationManager.cancel(1);
                islight = false;
            } else {
                bjiv.setImageResource(R.drawable.on);//点亮的图片
                turnOnFlashLight();
                notify_customview();
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
            mNotificationManager.cancel(1);
            islight = false;
            finish();
        }
    }
}

