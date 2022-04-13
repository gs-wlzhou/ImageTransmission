package com.android.api;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.RelativeLayout;

import com.android.api.utils.LogUtils;

public class UsbCameraManager {

    private ICameraService cameraService;
    private Activity activity;
    private View previewView;
    private CameraServiceCallback callback;

    // 服务开启状态
    private boolean status = false;

    // 预览尺寸
    private boolean isSmallSize = true;
    private int[] smallSize = {960, 540};
    private int[] largeSize = {1920, 1080};

    public UsbCameraManager(Builder builder) {
        CameraService.setPreviewView(builder.previewView);
        previewView = builder.previewView;
        callback = builder.cameraServiceCallback;
        activity = builder.activity;
        setPreviewSize(smallSize);
    }

    /**
     * 开启摄像头预览
     */
    public void startUsbCameraPreview() {
        if (status) {
            stopUsbCameraPreview();
        }
        LogUtils.d("start usb camera preview");
        bindCameraService();
        status = true;
    }

    /**
     * 停止摄像头预览
     */
    public void stopUsbCameraPreview() {
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
    public void previewSizeChange() {
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
    private void setPreviewSize(int[] size) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(size[0], size[1]);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        previewView.setLayoutParams(layoutParams);
        LogUtils.d("preview size change");
    }

    // 绑定服务
    private void bindCameraService() {
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
    private void unBindCameraService() {
        LogUtils.d("unBind camera service");
        Intent intent = new Intent(activity, CameraService.class);
        activity.unbindService(cameraServiceConnection);
        activity.stopService(intent);

        if (callback != null) {
            callback.onServiceStop();
        }
    }

    private ServiceConnection cameraServiceConnection = new ServiceConnection() {
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

    public static class Builder {

        private View previewView; // 预览画面显示位置
        private CameraServiceCallback cameraServiceCallback; // 服务启动|停止回调
        private Activity activity; // 上下文对象

        public Builder() {}

        public Builder previewView(View previewView) {
            this.previewView = previewView;
            return this;
        }

        public Builder cameraServiceCallback(CameraServiceCallback cameraServiceCallback) {
            this.cameraServiceCallback = cameraServiceCallback;
            return this;
        }

        public Builder activity(Activity activity) {
            this.activity = activity;
            return this;
        }

        public UsbCameraManager build() {
            return new UsbCameraManager(this);
        }
    }
}