package com.android.api;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;

public class CameraService extends Service implements UsbDeviceReceiver.UsbDeviceChangeListener {

    private CameraManager cameraManager; //摄像头管理
    private CameraDevice.StateCallback deviceCallback; //摄像头监听
    private CameraCaptureSession.CaptureCallback captureCallback; // 预览拍照监听
    private CameraCaptureSession cameraCaptureSession; // 控制摄像头预览或拍照
    private CameraDevice cameraDevice;

    private HandlerThread handlerThread;
    private Handler cameraHandler; // 后台处理图片传输帧

    private static volatile TextureView textureView;

    private UsbDeviceReceiver usbDeviceReceiver;

    public AudioRecord mAudioRecord;
    public AudioTrack mAudioTrack;
    private int recordBufferSize;
    private int trackBufferSize;
    private volatile boolean start;

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d("onCreate");
        registerUsbDeviceReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.d("onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d("onDestroy");
        unregisterUsbDeviceReceiver();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        LogUtils.d("CameraService onBind");
        CameraBinder cameraBinder = new CameraBinder();
        return cameraBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogUtils.d("CameraService onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onUsbDeviceAttached(UsbDevice device) {
        initData();
        initHandler();
        openCamera();
    }

    @Override
    public void onUsbDeviceDetached(UsbDevice device) {
        closeCamera();
        destroyHandler();
        stopPlay();
    }

    class CameraBinder extends ICameraService.Stub {

        @Override
        public void startUsbCameraPreview() throws RemoteException {
            initData();
            initHandler();
            openCamera();
        }

        @Override
        public void stopUsbCameraPreview() throws RemoteException {
            closeCamera();
            destroyHandler();
            stopPlay();
        }
    }

    // 设置预览画面显示
    public static void setTextureView(TextureView tv) {
        textureView = tv;
    }

    private void initData() {
        recordBufferSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                8000,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                recordBufferSize * 2);
        trackBufferSize = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                8000,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                trackBufferSize * 2,
                AudioTrack.MODE_STREAM);

        cameraManager = (CameraManager) CameraService.this.getSystemService(Context.CAMERA_SERVICE);

        deviceCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                cameraDevice = camera;
                takePreview();
                startPlay();
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {
                closeCamera();
            }

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {
                closeCamera();
                LogUtils.e("open camera error");
                Toast.makeText(CameraService.this, "open camera error", Toast.LENGTH_SHORT).show();
            }
        };

        captureCallback = new CameraCaptureSession.CaptureCallback() {
            @Override
            public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
                super.onCaptureStarted(session, request, timestamp, frameNumber);
            }
        };
    }

    // 为摄像头开一个线程
    private void initHandler() {
        handlerThread = new HandlerThread("camera");
        handlerThread.start();
        cameraHandler = new Handler(handlerThread.getLooper()); // handler与线程进行绑定
    }

    // 使用后置摄像头
    public void openCamera() {
        try {
            String[] cameraIdList = cameraManager.getCameraIdList();
            LogUtils.d("CameraIdList,length = " + cameraIdList.length);
            // 获取可用相机设备列表
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraIdList[0]);
            // 在这里可以通过CameraCharacteristics设置相机的功能,当然必须检查是否支持
            characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
            String cameraId = cameraIdList[0]; // 得到后摄像头编号
            LogUtils.d("cameraId = " + cameraId);
            cameraManager.openCamera(cameraId, deviceCallback, cameraHandler);
        } catch (Exception e) {
            Toast.makeText(CameraService.this, "please connect usb camera", Toast.LENGTH_SHORT).show();
            LogUtils.e("please connect usb camera");
        }
    }

    // 开启预览
    public void takePreview() {
        try {
            while (textureView == null) {
                LogUtils.w("wait textureView create");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            textureView.setScaleX(-1f);
            Surface surface = new Surface(textureView.getSurfaceTexture());
            CaptureRequest.Builder previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            // 将预览数据传递
            previewRequestBuilder.addTarget(surface);
            // 自动对焦
            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            // 打开闪光灯
            previewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    cameraCaptureSession = session;
                    CaptureRequest captureRequest = previewRequestBuilder.build();
                    try {
                        cameraCaptureSession.setRepeatingRequest(captureRequest, captureCallback, cameraHandler);
                    } catch (CameraAccessException e) {
                        Toast.makeText(CameraService.this, "camera access exception", Toast.LENGTH_SHORT).show();
                        LogUtils.e("camera access exception");
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            }, null);
        } catch (CameraAccessException e) {
            Toast.makeText(CameraService.this, "camera access exception", Toast.LENGTH_SHORT).show();
            LogUtils.e("camera access exception");
        }
    }

    // 关闭摄像头
    public void closeCamera() {
        if (cameraDevice == null) {
            return;
        }
        cameraDevice.close();
        cameraDevice = null;
    }

    // 关闭线程
    private void destroyHandler() {
        if (handlerThread == null) {
            return;
        }
        handlerThread.quitSafely();
        try {
            handlerThread.join(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 音频播放
    public void startPlay() {
        start = true;
        LogUtils.d("start play -> ok");
        new Thread(new Runnable() {
            @Override
            public void run() {
                mAudioRecord.startRecording();
                mAudioTrack.play();
                byte[] bytes = new byte[2048];
                while (start) {
                    int len = mAudioRecord.read(bytes, 0, bytes.length);
                    LogUtils.v("mAudioRecord read,len = " + len);
                    byte[] temp = new byte[2048];
                    System.arraycopy(bytes, 0, temp, 0, len);
                    mAudioTrack.write(temp, 0, len);
                    LogUtils.v("mAudioTrack write,len = " + len);
                }
                mAudioRecord.stop();
                mAudioRecord.release();
                mAudioTrack.stop();
                mAudioTrack.release();
            }
        }).start();
    }

    // 音频停止
    public void stopPlay() {
        start = false;
        LogUtils.d("stop play -> ok");
    }

    // 注册广播监听取消
    public void registerUsbDeviceReceiver() {
        if (usbDeviceReceiver == null) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
            intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
            usbDeviceReceiver = new UsbDeviceReceiver(CameraService.this);
            registerReceiver(usbDeviceReceiver, intentFilter);
            LogUtils.d("registerUsbDeviceReceiver");
        }
    }

    // 取消广播监听
    public void unregisterUsbDeviceReceiver() {
        if (usbDeviceReceiver != null) {
            unregisterReceiver(usbDeviceReceiver);
            LogUtils.d("unregisterUsbDeviceReceiver");
        }
    }
}