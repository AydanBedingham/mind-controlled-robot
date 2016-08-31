package com.usbhosttest.managers;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.usbhosttest.UsbEndpointCombo;
import com.usbhosttest.command.Command;
import com.usbhosttest.utils.ThreadUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SupremeUsbManager extends AbstractManager<SupremeUsbManagerListener> {
    private Toast toast;
    private final SupremeUsbManager me = this;

    //Constants
    private static final String TAG = SupremeUsbManager.class.toString();
    public static final int THREAD_EXECUTION_INTERVAL = 3000; //Milliseconds
    public static final int DATA_SEND_TIMEOUT = 300;
    private static final String ACTION_USB_PERMISSION = SupremeUsbManager.class.toString();

    private Date lastSampleDate;

    //Private members
    private UsbDevice device;
    private UsbInterface dataInterface;
    private UsbDeviceConnection connection;
    private UsbEndpointCombo usbEndpoints;
    private Context context;
    private AsyncTask<Void, Integer, List<Byte>> listeningTask;

    private int vendorId;
    private int productId;

    public int getVendorId() {
        return vendorId;
    }

    public void setVendorId(int vendorId) {
        this.vendorId = vendorId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public SupremeUsbManager(Context context){
        super();
        this.context = context;
    }


    @Override
    protected void onManagerStateChanged(ManagerState oldState, ManagerState newState) {
        if ((oldState!=ManagerState.Working)&&(newState==ManagerState.Working)) {
            SupremeUsbHelper.onSupremeUsbManagerConnect(this, this.device, this.getListeners());
        } else if ((oldState==ManagerState.Working)&&(newState==ManagerState.NotWorking)) {
            SupremeUsbHelper.onSupremeUsbManagerDisconnect(this, this.device, this.getListeners());
        }
    }


    @Override
    protected void onDidStart() {
        this.device = null;
        this.connection = null;
        this.usbEndpoints = null;

        this.setManagerState(ManagerState.Initialising);

        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        this.context.registerReceiver(mUsbReceiver, filter);

        readData();
    }

    private boolean requested = false;

    @Override
    protected void onExecute() {
        Date currentDate = new Date();
        long millisecondsSinceLastUpdate = lastSampleDate!=null ? currentDate.getTime() - lastSampleDate.getTime() : THREAD_EXECUTION_INTERVAL;

        if (millisecondsSinceLastUpdate>=THREAD_EXECUTION_INTERVAL){
            this.execute();
            this.lastSampleDate = currentDate;
        }
        ThreadUtil.sleep(100);
    }

    private void execute(){
        try{
            UsbManager usbManager = (UsbManager) this.context.getSystemService(Context.USB_SERVICE);
            this.device = SupremeUsbHelper.findDevice(vendorId, productId, usbManager);

            if (usbManager.hasPermission(this.device)){
                this.dataInterface = this.device.getInterface(1);
                this.usbEndpoints = SupremeUsbHelper.findEndpoints(dataInterface);
                this.connection = usbManager.openDevice(this.device);
                connection.claimInterface(dataInterface, true);
                this.setManagerState(ManagerState.Working);
            } else{
                PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this.context, 0, new Intent(ACTION_USB_PERMISSION), 0);
                usbManager.requestPermission(device, mPermissionIntent);
                setManagerState(ManagerState.Initialising);
            }
        } catch(Exception e){
            this.setError(e);
            this.setManagerState(ManagerState.NotWorking);
            SupremeUsbHelper.onSupremeUsbManagerError(this, e, this.getListeners());
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    public void sendCommand(Command command) throws IOException{
        byte[] bytes = Command.commandToBytes(command);
        this.sendBytes(bytes);
    }

    public void sendBytes(byte[] bytes){
        this.connection.bulkTransfer(this.usbEndpoints.getEndpointOut(), bytes, bytes.length, DATA_SEND_TIMEOUT);
    }


    @Override
    protected void onDidStop() {
        this.context.unregisterReceiver(mUsbReceiver);
        if (listeningTask!=null) { this.listeningTask.cancel(true); }
        this.setManagerState(ManagerState.NotWorking);
    }


    ////


    private void readData() {


        final SupremeUsbManager manager = this;

        this.listeningTask = new AsyncTask<Void, Integer, List<Byte>>() {
            private boolean isCancelled = false;

            @Override
            protected  List<Byte> doInBackground(Void... params) {

                List<Byte> byteList = new ArrayList<Byte>();

                try {
                    String msg = "";
                    while (true) {
                        if (isCancelled) return null;
                        if (manager.getManagerState() == ManagerState.Working) {
                            byte[] mReadBuffer = new byte[64];
                            int numBytesRead = manager.connection.bulkTransfer(manager.usbEndpoints.getEndpointIn(), mReadBuffer, mReadBuffer.length, DATA_SEND_TIMEOUT);

                            if (numBytesRead > 0) {
                                for (int i = 0; i < numBytesRead; i++) {
                                    if (mReadBuffer[i]=='\n') {
                                        return byteList;
                                    }
                                    byte aByte = mReadBuffer[i];
                                    byteList.add(aByte);
                                }
                            }
                        } else {
                            byteList.clear();
                        }
                    }
                } catch (Exception e) {
                    //Suppress exception
                }
                return null;
            }

            @Override
            protected void onPostExecute( List<Byte> byteList) {
                if ((byteList!=null)&&(byteList.size()>0)) {

                    for(SupremeUsbManagerListener listener : manager.getListeners()){
                        byte[] byteArray = new byte[byteList.size()];
                        for (int i=0; i<byteArray.length; i++){
                            byteArray[i] = byteList.get(i);
                        }
                        String msg = new String(byteArray);
                        Command aCommand = new Command(msg);
                        SupremeUsbHelper.onSupremeUsbManagerReceiveData(manager, device, aCommand, manager.getListeners());

                        /*
                        Command command = null;
                        try{
                            command = Command.bytesToCommand(msg.getBytes());
                        } catch(IOException e){
                            command = new Command(msg);
                            //command = new Command(e.getLocalizedMessage());
                            String str = new String(msg.getBytes());
                            Log.e(TAG, "Invalid command structure: " + str);
                        }
                        SupremeUsbHelper.onSupremeUsbManagerReceiveData(manager, device, command, manager.getListeners());
                        */
                    }
                    readData();
                }
            }

            @Override
            protected void onCancelled() {
                isCancelled = true;
            }
        };
        this.listeningTask.execute();
    }


    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    try{
                        if (!intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            throw new Exception("Permission denied for device :" + device);
                        }
                    } catch(Exception e){
                        SupremeUsbHelper.onSupremeUsbManagerError(me, e, me.getListeners());
                    }
                }
            }
        }
    };

}
