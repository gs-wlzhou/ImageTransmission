package com.android.api;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.TextureView;
import android.widget.RelativeLayout;

public class UsbCameraManager {

    private static ICameraService cameraService;
    private static volatile Activity activity;
    private static volatile TextureView textureView;
    private static volatile CameraServiceCallback callback;

    // 服务开启状态
    private static volatile boolean status = false;

    // 预览尺寸
    private static boolean isSmallSize;
    private static int[] smallSize = {960, 540};
    private static int[] largeSize = {1920, 1080};

    private UsbCameraManager() {}

    /**
     * 设置服务启动/停止回调
     * @param c
     */
    public static void setCameraServiceCallback(CameraServiceCallback c) {
        callback = c;
    }

    /**
     * 设置预览画面显示位置
     * @param tv
     */
    public static void setTextureView(TextureView tv) {
        textureView = tv;
        LogUtils.d("set texture view");
        CameraService.setTextureView(tv);
    }

    /**
     * 开启摄像头预览
     * @param a
     */
    public static void startUsbCameraPreview(Activity a) {
        if (status) {
            stopUsbCameraPreview();
        }
        LogUtils.d("start usb camera preview");
        activity = a;
        bindCameraService();
        status = true;
    }

    /**
     * 停止摄像头预览
     */
    public static void stopUsbCameraPreview() {
        if (!status) {
            return;
        }
        LogUtils.d("stop usb camera preview");
        try {
            cameraService.stopUsbCameraPreview();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        unBindCameraService();
        status = false;
    }

    /**
     * 预览尺寸切换
     */
    public static void previewSizeChange() {
        int[] curSize = null;
        if (isSmallSize) {
            isSmallSize = false;
            curSize = largeSize;
        } else {
            isSmallSize = true;
            curSize = smallSize;
        }
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(curSize[0], curSize[1]);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        textureView.setLayoutParams(layoutParams);
    }

    // 绑定服务
    private static void bindCameraService() {
        LogUtils.d("bind camera service");
        Intent intent = new Intent(activity, CameraService.class);
        activity.startService(intent);
        boolean flag = activity.bindService(intent, cameraServiceConnection, Context.BIND_AUTO_CREATE);
        LogUtils.d("isBind -> " + flag);

        if (flag && callback != null) {
            callback.onServiceStart();
        }
    }

    // 解绑服务
    private static void unBindCameraService() {
        LogUtils.d("unBind camera service");
        Intent intent = new Intent(activity, CameraService.class);
        activity.unbindService(cameraServiceConnection);
        activity.stopService(intent);

        if (callback != null) {
            callback.onServiceStop();
        }
    }

    private static ServiceConnection cameraServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            LogUtils.d("onServiceConnected");
            cameraService = ICameraService.Stub.asInterface(iBinder);

            // 开启摄像头预览
            try {
                cameraService.startUsbCameraPreview();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            LogUtils.d("onServiceDisconnected");

            // 停止摄像头预览
            try {
                cameraService.stopUsbCameraPreview();
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            cameraService = null;
        }
    };

    public interface CameraServiceCallback {

        /**
         * 服务启动
         */
        void onServiceStart();

        /**
         * 服务停止
         */
        void onServiceStop();
    }
}