package com.android.camera;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.android.api.LogUtils;
import com.android.api.UsbCameraManager;

import jp.co.cyberagent.android.gpuimage.GPUImageView;

public class CameraActivity extends Activity {

    private GPUImageView gpuImageView;
    private Button startBtn;
    private Button stopBtn;
    private Button changeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d("onCreate");
        requestWindowFeature(Window.FEATURE_NO_TITLE); // 设置窗口没有标题
        setContentView(R.layout.activity_camera);
        initView();
        UsbCameraManager.initConfig(gpuImageView, true, callback); // 初始化配置
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtils.d("onStop");
        UsbCameraManager.stopUsbCameraPreview(); // 停止预览
    }

    private void initView() {
        gpuImageView = findViewById(R.id.iv);
        startBtn = findViewById(R.id.start);
        stopBtn = findViewById(R.id.stop);
        changeBtn = findViewById(R.id.change);
        startBtn.setOnClickListener(onClickListener);
        stopBtn.setOnClickListener(onClickListener);
        changeBtn.setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.start:
                    UsbCameraManager.startUsbCameraPreview(CameraActivity.this); // 开启预览
                    break;
                case R.id.stop:
                    UsbCameraManager.stopUsbCameraPreview(); // 停止预览
                    break;
                case R.id.change:
                    UsbCameraManager.previewSizeChange(); // 预览尺寸切换
                    break;
            }
        }
    };

    private UsbCameraManager.CameraServiceCallback callback = new UsbCameraManager.CameraServiceCallback() {
        @Override
        public void onServiceStart() {
            Toast.makeText(CameraActivity.this, "service start", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceStop() {
            Toast.makeText(CameraActivity.this, "service stop", Toast.LENGTH_SHORT).show();
        }
    };
}