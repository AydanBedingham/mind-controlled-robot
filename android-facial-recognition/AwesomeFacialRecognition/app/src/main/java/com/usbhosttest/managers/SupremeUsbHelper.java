package com.usbhosttest.managers;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import com.usbhosttest.UsbEndpointCombo;
import com.usbhosttest.command.Command;

import java.io.IOException;
import java.util.List;

public class SupremeUsbHelper {

    public static UsbDevice findDevice(int vendorId, int productId, UsbManager manager) throws IOException {
        UsbDevice device = null;

        for (UsbDevice d : manager.getDeviceList().values()) {
            boolean matchFound = true;
            if ((vendorId!=0)&&(d.getVendorId()!=vendorId)) { matchFound = false; }
            if ((productId!=0)&&(d.getProductId()!=productId)) { matchFound = false; }
            if (matchFound==true){ device = d; }
        }
        if (device!=null) {
            return device;
        } else {
            throw new IOException("Device not found");
        }
    }

    public static UsbEndpointCombo findEndpoints(UsbInterface dataInterface) throws IOException {
        UsbEndpointCombo usbEndpointCombo = new UsbEndpointCombo();

        for (int j=0; j<dataInterface.getEndpointCount(); j++) {
            UsbEndpoint ep = dataInterface.getEndpoint(j);
            if (ep.getDirection() == UsbConstants.USB_DIR_OUT &&
                    ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {

                UsbEndpoint endpointOut = dataInterface.getEndpoint(j);
                usbEndpointCombo.setEndpointOut(endpointOut);
            }
            if (ep.getDirection() == UsbConstants.USB_DIR_IN &&
                    ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {

                UsbEndpoint endpointIn = dataInterface.getEndpoint(j);
                usbEndpointCombo.setEndpointIn(endpointIn);
            }
        }
        if ((usbEndpointCombo.getEndpointIn()==null)){
            throw new IOException("EndpointIn not found");
        } else if ((usbEndpointCombo.getEndpointOut()==null)){
            throw new IOException("EndpointOut not found");
        }
        return usbEndpointCombo;
    }

    ///


    public static void onSupremeUsbManagerConnect(final SupremeUsbManager sender, final UsbDevice device, List<SupremeUsbManagerListener> listeners) {
        for (final SupremeUsbManagerListener listener : listeners) {
            if (listener.getActivity() != null) {
                listener.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        listener.onSupremeUsbManagerConnect(sender, device);
                    }
                });
            }
        }
    }

    public static void onSupremeUsbManagerDisconnect(final SupremeUsbManager sender, final UsbDevice device, List<SupremeUsbManagerListener> listeners){
        for (final SupremeUsbManagerListener listener : listeners) {
            if (listener.getActivity() != null) {
                listener.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        listener.onSupremeUsbManagerDisconnect(sender, device);
                    }
                });
            }
        }
    }

    public static void onSupremeUsbManagerReceiveData(final SupremeUsbManager sender, final UsbDevice device, final Command command, List<SupremeUsbManagerListener> listeners){
        for (final SupremeUsbManagerListener listener : listeners) {
            if (listener.getActivity() != null) {
                listener.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        listener.onSupremeUsbManagerReceiveData(sender, device, command);
                    }
                });
            }
        }
    }

    public static void onSupremeUsbManagerError(final SupremeUsbManager sender, final Throwable error, final List<SupremeUsbManagerListener> listeners){
        for (final SupremeUsbManagerListener listener : listeners) {
            if (listener.getActivity() != null) {
                listener.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        listener.onSupremeUsbManagerError(sender, error);
                    }
                });
            }
        }
    }



}

