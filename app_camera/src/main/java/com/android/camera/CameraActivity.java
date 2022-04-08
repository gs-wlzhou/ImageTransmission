package com.android.camera;

import androidx.annotation.NonNull;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.android.api.LogUtils;
import com.android.api.UsbCameraManager;

public class CameraActivity extends Activity {

    private TextureView textureView;
    private Button startBtn;
    private Button stopBtn;
    private Button changeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d("onCreate");
        requestWindowFeature(Window.FEATURE_NO_TITLE); // 设置窗口没有标题
        // 可选
        UsbCameraManager.setCameraServiceCallback(callback);
        setContentView(R.layout.activity_camera);
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogUtils.d("onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.d("onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtils.d("onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtils.d("onStop");
        // 停止预览
        UsbCameraManager.stopUsbCameraPreview();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.d("onDestroy");
    }

    private void initView() {
        textureView = findViewById(R.id.tv);
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
                LogUtils.d("texture view created");
                // 预览显示
                UsbCameraManager.setTextureView(textureView);
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
        startBtn = findViewById(R.id.start);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 开启预览
                UsbCameraManager.startUsbCameraPreview(CameraActivity.this);
            }
        });
        stopBtn = findViewById(R.id.stop);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 停止预览
                UsbCameraManager.stopUsbCameraPreview();
            }
        });
        changeBtn = findViewById(R.id.change);
        changeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 预览尺寸切换
                UsbCameraManager.previewSizeChange();
            }
        });
    }

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