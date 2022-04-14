package com.android.usbcamera;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.TextureView;
import android.view.View;
import android.widget.RelativeLayout;

import com.android.usbcamera.utils.LogUtils;

import jp.co.cyberagent.android.gpuimage.GPUImageView;

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
        CameraService.setResolution(builder.resolution);
        CameraService.setFrameRate(builder.frameRate);
        LogUtils.setTAG(builder.tag);
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

    // 调整锐化值
    public void sharpenChange(Integer progress) {
        CameraService.sharpenChange(progress);
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

    /**
     * 服务状态回调接口
     */
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

    /**
     * Usb摄像头管理构造器
     */
    public static class Builder {

        private View previewView; // 预览画面显示位置
        private CameraServiceCallback cameraServiceCallback; // 服务启动|停止回调
        private Activity activity; // 上下文对象
        private String resolution = UsbCameraConstant.RESOLUTION_720; // 分辨率
        private String frameRate = UsbCameraConstant.FRAME_RATE_LOW; // 输出帧率
        private String tag = "confcameracontrol"; // 日志tag

        public Builder() {}

        /**
         * 使用TextureView作为画面预览(必须设置)
         * @param previewView
         * @return Builder
         */
        public Builder previewView(TextureView previewView) {
            this.previewView = previewView;
            return this;
        }

        /**
         * 使用GPUImageView作为画面预览(必须设置)
         * @param previewView
         * @return Builder
         */
        public Builder previewView(GPUImageView previewView) {
            this.previewView = previewView;
            return this;
        }

        /**
         * 设置服务启动停止回调
         * @param cameraServiceCallback
         * @return Builder
         */
        public Builder cameraServiceCallback(CameraServiceCallback cameraServiceCallback) {
            this.cameraServiceCallback = cameraServiceCallback;
            return this;
        }

        /**
         * 设置Activity上下文(必须设置)
         * @param activity
         * @return Builder
         */
        public Builder activity(Activity activity) {
            this.activity = activity;
            return this;
        }

        /**
         * 设置分辨率
         * “720”低分辨率,“1080”高分辨率
         * @param resolution
         * @return Builder
         */
        public Builder resolution(String resolution) {
            this.resolution = resolution;
            return this;
        }

        /**
         * 设置输出帧率(GPUImageView预览才设置)
         * “low”低帧率,“medium”中帧率,“high”高帧率;
         * @param frameRate
         * @return Builder
         */
        public Builder frameRate(String frameRate) {
            this.frameRate = frameRate;
            return this;
        }

        /**
         * 设置日志tag
         * @param tag
         * @return Builder
         */
        public Builder tag(String tag) {
            this.tag = tag;
            return this;
        }

        public UsbCameraManager build() {
            return new UsbCameraManager(this);
        }
    }
}