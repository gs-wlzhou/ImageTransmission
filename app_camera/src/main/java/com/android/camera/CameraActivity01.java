package com.android.camera;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.api.CameraService;
import com.android.api.UsbCameraManager;

public class CameraActivity01 extends Activity {

    private TextureView textureView;
    private Button startBtn;
    private Button stopBtn;
    private Button changeBtn;
    private UsbCameraManager usbCameraManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // 设置窗口没有标题
        setContentView(R.layout.activity_camera01);
        initView();
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
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
                usbCameraManager = new UsbCameraManager.Builder()
                        .resolution("1080")
                        .previewView(textureView)
                        .cameraServiceCallback(callback)
                        .activity(CameraActivity01.this).build();
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {

            }
        });
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
            Toast.makeText(CameraActivity01.this, "service start", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceStop() {
            Toast.makeText(CameraActivity01.this, "service stop", Toast.LENGTH_SHORT).show();
        }
    };
}