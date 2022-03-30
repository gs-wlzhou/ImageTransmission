package com.android.api;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.TextureView;

public class UsbCameraManager {

    private static ICameraService cameraService;
    private static volatile Activity activity;

    private UsbCameraManager() {}

    public static void setTextureView(TextureView tv) {
        CameraService.setTextureView(tv);
    }

    public static void startUsbCameraPreview(Activity a) {
        activity = a;
        bindCameraService();
    }

    public static void stopUsbCameraPreview() {
        try {
            cameraService.stopUsbCameraPreview();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        unBindCameraService();
    }

    private static void bindCameraService() {
        Intent intent = new Intent(activity, CameraService.class);
        activity.startService(intent);
        boolean flag = activity.bindService(intent, cameraServiceConnection, Context.BIND_AUTO_CREATE);
        LogUtils.d("isBind -> " + flag);
    }

    private static void unBindCameraService() {
        Intent intent = new Intent(activity, CameraService.class);
        activity.unbindService(cameraServiceConnection);
        activity.stopService(intent);
    }

    private static ServiceConnection cameraServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            LogUtils.d("onServiceConnected");
            cameraService = ICameraService.Stub.asInterface(iBinder);

            try {
                cameraService.startUsbCameraPreview();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            LogUtils.d("onServiceDisconnected");

            try {
                cameraService.stopUsbCameraPreview();
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            cameraService = null;
        }
    };
}