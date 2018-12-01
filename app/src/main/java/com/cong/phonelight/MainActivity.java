package com.cong.phonelight;

import java.util.Timer;
import java.util.TimerTask;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView iv, bjiv;
    private boolean mBackKeyPressed = false;//记录是否有首次按键
    private CameraManager mCameraManager;
    private String mCameraId;
    private boolean islight = false;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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

    private void initView() {
        iv = findViewById(R.id.iv);
        bjiv = findViewById(R.id.bjiv);
        iv.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        try {
            if (islight) {
                bjiv.setImageResource(R.drawable.off);//关闭的图片
                turnOffFlashLight();
                islight = false;
            } else {
                bjiv.setImageResource(R.drawable.on);//点亮的图片
                turnOnFlashLight();
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
            islight = false;
            finish();
        }
    }
}

