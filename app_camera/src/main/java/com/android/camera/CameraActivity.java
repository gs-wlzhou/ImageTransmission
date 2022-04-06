package com.android.camera;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.api.LogUtils;
import com.android.api.UsbCameraManager;

public class CameraActivity extends AppCompatActivity {

    /*private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
    private static final int REQUEST_PERMISSIONS_CODE = 1;*/

    private TextureView textureView;
    private Button startBtn;
    private Button stopBtn;
    private Button changeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 可选
        UsbCameraManager.setCameraServiceCallback(callback);
        LogUtils.d("onCreate");
        hideTitle();
        setContentView(R.layout.activity_camera);
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogUtils.d("onStart");
//        initPermission();
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

    // 设置窗口没有标题
    private void hideTitle() {
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
    }

    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    LogUtils.d("no permission");
                    Toast.makeText(CameraActivity.this, "no permission", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
        // 开启预览
        UsbCameraManager.startUsbCameraPreview(CameraActivity.this);
    }*/

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

    // 判断权限
    /*private void initPermission() {
        LogUtils.d("initPermission");
        if (!hasPermissions()) {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_PERMISSIONS_CODE);
        } else {
            // 开启预览
            UsbCameraManager.startUsbCameraPreview(CameraActivity.this);
        }
    }*/

    /*private boolean hasPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(CameraActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }*/

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