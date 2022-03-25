package com.android.api;

interface ICameraService {

    void startC03Preview();

    void stopC03Preview();

    void registerUsbDeviceReceiver();

    void unregisterUsbDeviceReceiver();
}