package com.android.api;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.RelativeLayout;

import jp.co.cyberagent.android.gpuimage.GPUImageView;

public class UsbCameraManager {

    private static ICameraService cameraService;
    private static volatile Activity activity;
    private static volatile GPUImageView imageView;
    private static volatile CameraServiceCallback callback;

    // 服务开启状态
    private static volatile boolean status = false;

    // 预览尺寸
    private static boolean isSmallSize = true;
    private static int[] smallSize = {960, 540};
    private static int[] largeSize = {1920, 1080};

    private UsbCameraManager() {}

    /**
     * 初始化配置
     * @param gpuImageView 预览画面显示位置
     * @param isImageSharpen 图像锐化
     * @param cameraServiceCallback 服务启动/停止回调
     */
    public static void initConfig(GPUImageView gpuImageView, boolean isImageSharpen, CameraServiceCallback cameraServiceCallback) {
        CameraService.setGpuImageView(gpuImageView);
        CameraService.setImageSharpen(isImageSharpen);
        imageView = gpuImageView;
        callback = cameraServiceCallback;
        setPreviewSize(smallSize);
    }

    /**
     * 开启摄像头预览
     * @param a Activity上下文
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
        if (isSmallSize) {
            isSmallSize = false;
            setPreviewSize(largeSize);
            LogUtils.d("set large size");
        } else {
            isSmallSize = true;
            setPreviewSize(smallSize);
            LogUtils.d("set small size");
        }
    }

    // 设置预览尺寸
    private static void setPreviewSize(int[] size) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(size[0], size[1]);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        imageView.setLayoutParams(layoutParams);
        LogUtils.d("preview size change");
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