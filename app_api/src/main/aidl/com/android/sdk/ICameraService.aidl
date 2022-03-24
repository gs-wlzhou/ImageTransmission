package com.android.sdk;

interface ICameraService {

    void startC03Preview();

    void stopC03Preview();

    void registerUsbDeviceReceiver();

    void unregisterUsbDeviceReceiver();
}