package com.usbhosttest.managers;

import android.app.Activity;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;

import com.usbhosttest.UsbEndpointCombo;
import com.usbhosttest.command.Command;

import java.util.List;

public interface SupremeUsbManagerListener {

    public void onSupremeUsbManagerConnect(SupremeUsbManager sender, UsbDevice device);

    public void onSupremeUsbManagerDisconnect(SupremeUsbManager sender, UsbDevice device);

    public void onSupremeUsbManagerReceiveData(SupremeUsbManager sender, UsbDevice device, Command command);

    public void onSupremeUsbManagerError(SupremeUsbManager sender, Throwable error);

    public Activity getActivity();
}
