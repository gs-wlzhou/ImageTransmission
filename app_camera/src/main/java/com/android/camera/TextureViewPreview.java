package com.android.camera;

import android.app.Activity;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.android.usbcamera.UsbCameraConfig;
import com.android.usbcamera.UsbCameraConstant;
import com.android.usbcamera.UsbCameraManager;

public class TextureViewPreview extends Activity {

    private TextureView textureView;
    private Button startBtn;
    private Button stopBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // 设置窗口没有标题
        setContentView(R.layout.preview_textureview);
        initView();
        UsbCameraConfig usbCameraConfig = new UsbCameraConfig.Builder()
                .setPreviewView(textureView) // 设置预览视图
                .setActivity(TextureViewPreview.this) // 设置当前activity上下文
                .setResolution(UsbCameraConstant.RESOLUTION_720) // 设置分辨率
                .setPreviewSize(UsbCameraConstant.SIZE_SMALL) // 设置预览尺寸
                .setCameraServiceCallback(callback) // 设置服务启动|停止状态回调
                .setTag("tag01")
                .build();
        UsbCameraManager.init(usbCameraConfig);
    }

    @Override
    protected void onStop() {
        super.onStop();
        UsbCameraManager.stopUsbCameraPreview(); // 停止预览
    }

    private void initView() {
        startBtn = findViewById(R.id.start);
        stopBtn = findViewById(R.id.stop);
        startBtn.setOnClickListener(onClickListener);
        stopBtn.setOnClickListener(onClickListener);
        textureView = findViewById(R.id.tv);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.start:
                    UsbCameraManager.startUsbCameraPreview(); // 开启预览
                    break;
                case R.id.stop:
                    UsbCameraManager.stopUsbCameraPreview(); // 停止预览
                    break;
            }
        }
    };

    private UsbCameraManager.CameraServiceCallback callback = new UsbCameraManager.CameraServiceCallback() { // 服务启动|停止回调
        @Override
        public void onServiceStart() {
            Toast.makeText(TextureViewPreview.this, "service start", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceStop() {
            Toast.makeText(TextureViewPreview.this, "service stop", Toast.LENGTH_SHORT).show();
        }
    };
}