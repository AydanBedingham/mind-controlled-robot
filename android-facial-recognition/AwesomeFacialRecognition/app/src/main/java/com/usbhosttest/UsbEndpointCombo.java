package com.usbhosttest;

import android.hardware.usb.UsbEndpoint;

public class UsbEndpointCombo {
    private UsbEndpoint endpointOut;
    private UsbEndpoint endpointIn;

    public UsbEndpoint getEndpointOut() {
        return endpointOut;
    }

    public void setEndpointOut(UsbEndpoint endpointOut) {
        this.endpointOut = endpointOut;
    }

    public UsbEndpoint getEndpointIn() {
        return endpointIn;
    }

    public void setEndpointIn(UsbEndpoint endpointIn) {
        this.endpointIn = endpointIn;
    }
}
