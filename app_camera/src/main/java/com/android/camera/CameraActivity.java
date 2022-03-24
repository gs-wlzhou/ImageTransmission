package com.android.camera;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.TextureView;
import android.widget.Toast;

import com.android.api.CameraService;
import com.android.sdk.ICameraService;

public class CameraActivity extends AppCompatActivity {

    private static final String TAG = "wlzhou";
    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
    private static final int REQUEST_PERMISSIONS_CODE = 1;

    private TextureView textureView;

    private ICameraService cameraService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "[onCreate]");
        super.onCreate(savedInstanceState);
        hideTitle();
        setContentView(R.layout.activity_camera);
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "[onStart]");
        // 绑定服务
        bindCameraService();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "[onResume]");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "[onPause]");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "[onStop]");
        super.onStop();
        try {
            cameraService.stopC03Preview();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        // 解绑服务
        unBindCameraService();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "[onDestroy]");
        super.onDestroy();
    }

    // 设置窗口没有标题
    private void hideTitle() {
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean flag = true;
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) {
                    flag = false;
                    break;
                }
            }
        }
        if (flag) {
            try {
                cameraService.startC03Preview();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(CameraActivity.this, "no permission", Toast.LENGTH_SHORT).show();
        }
    }

    private void initView() {
        textureView = findViewById(R.id.tv);
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
                Toast.makeText(CameraActivity.this, "created", Toast.LENGTH_SHORT).show();
                CameraService.setTextureView(textureView);
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

    private boolean hasPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(CameraActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    private void bindCameraService() {
        Intent intent = new Intent(this, CameraService.class);
        startService(intent);
        boolean b = bindService(intent, cameraServiceConnection, BIND_AUTO_CREATE);
        Log.d(TAG, "[bindCameraService] isBind -> " + b);
    }

    private void unBindCameraService() {
        Intent intent = new Intent(this, CameraService.class);
        unbindService(cameraServiceConnection);
        stopService(intent);
    }

    private ServiceConnection cameraServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "[onServiceConnected]");
            cameraService = ICameraService.Stub.asInterface(iBinder);

            // 服务绑定成功后监听广播
            try {
                cameraService.registerUsbDeviceReceiver();
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            if (!hasPermissions()) {
                requestPermissions(REQUIRED_PERMISSIONS, REQUEST_PERMISSIONS_CODE);
            } else {
                try {
                    cameraService.startC03Preview();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected");

            // 服务解绑后取消监听广播
            try {
                cameraService.unregisterUsbDeviceReceiver();
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            cameraService = null;
        }
    };
}