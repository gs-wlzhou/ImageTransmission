package com.android.camera;

import android.app.Activity;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.android.usbcamera.UsbCameraConstant;
import com.android.usbcamera.UsbCameraManager;

public class TextureViewPreview extends Activity {

    private TextureView textureView;
    private Button startBtn;
    private Button stopBtn;
    private Button changeBtn;
    private UsbCameraManager usbCameraManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // 设置窗口没有标题
        setContentView(R.layout.preview_textureview);
        initView();
        usbCameraManager = new UsbCameraManager.Builder()
                .previewView(textureView) // 设置预览视图
                .activity(TextureViewPreview.this) // 设置当前activity上下文
                .resolution(UsbCameraConstant.RESOLUTION_720) // 设置分辨率
                .cameraServiceCallback(callback) // 设置服务状态回调
                .tag("tag01") // 设置日志tag
                .build();
    }

    @Override
    protected void onStop() {
        super.onStop();
        usbCameraManager.stopUsbCameraPreview(); // 停止预览
    }

    private void initView() {
        startBtn = findViewById(R.id.start);
        stopBtn = findViewById(R.id.stop);
        changeBtn = findViewById(R.id.change);
        startBtn.setOnClickListener(onClickListener);
        stopBtn.setOnClickListener(onClickListener);
        changeBtn.setOnClickListener(onClickListener);
        textureView = findViewById(R.id.tv);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.start:
                    usbCameraManager.startUsbCameraPreview(); // 开启预览
                    break;
                case R.id.stop:
                    usbCameraManager.stopUsbCameraPreview(); // 停止预览
                    break;
                case R.id.change:
                    usbCameraManager.previewSizeChange(); // 预览尺寸切换
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