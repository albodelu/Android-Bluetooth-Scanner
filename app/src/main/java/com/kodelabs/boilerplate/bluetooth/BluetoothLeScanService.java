package com.kodelabs.boilerplate.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;


/**
 * Created by dmilicic on 7/28/15.
 * <p/>
 * This Android service is responsible for scanning for Bluetooth devices and broadcasting new updates to the system.
 * The one who wants to listen for scanned devices should register for the appropriate broadcast action.
 */
public class BluetoothLeScanService extends Service implements BluetoothAdapter.LeScanCallback {

    private static final long SCAN_PERIOD = 10000; // in milliseconds

    private BluetoothAdapter mBluetoothAdapter;
    private boolean          mScanning;
    private Handler          mHandler;

    // Messenger for IPC communication
    private final Messenger mMessenger = new Messenger(new IncomingHandler());

    // Message types
    public static final int MSG_SCAN_DEVICES = 1;

    // Actions
    public static final String ACTION_FOUND_DEVICE = "action_found_device";
    public static final String ACTION_STOP_SCAN    = "action_stop_scan";

    // Extra key
    public static final String EXTRA_DEVICE_NAME    = "bluetooth_device_name";
    public static final String EXTRA_DEVICE_ADDRESS = "bluetooth_device_address";
    public static final String EXTRA_DEVICE_RSSI    = "bluetooth_device_rssi";

    private Map<String, BluetoothDevice> mDevices;

    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SCAN_DEVICES:
                    initializeScan();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void initializeScan() {
        mHandler = new Handler(getMainLooper());
        mDevices = new HashMap<>();

        Timber.w("Process ID: " + android.os.Process.myPid());
        Timber.i("Starting bluetooth scanning service...");
        Timber.i("THREAD: " + Thread.currentThread().getName());

        scanLeDevice();
    }


    @Override
    public void onCreate() {
        super.onCreate();

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.w("DESTROYING SCAN SERVICE");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Timber.d("Binding to scan service..." + " THREAD: " + Thread.currentThread().getName());
        return mMessenger.getBinder();
    }

    private void broadcastUpdate(BluetoothDevice device, int rssi) {
        final Intent intent = new Intent(ACTION_FOUND_DEVICE);

        intent.putExtra(EXTRA_DEVICE_NAME, device.getName());
        intent.putExtra(EXTRA_DEVICE_ADDRESS, device.getAddress());
        intent.putExtra(EXTRA_DEVICE_RSSI, rssi);

        sendBroadcast(intent);
    }

    private void broadcastStopScanUpdate() {
        final Intent intent = new Intent(ACTION_STOP_SCAN);
        sendBroadcast(intent);
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

        broadcastUpdate(device, rssi);

        Timber.i("***** New Device found *****");
        Timber.i("BluetoothDevice: " + device);
        Timber.i("DeviceName     : " + device.getName());
        Timber.i("RSSI           : " + rssi);

        if (scanRecord != null && scanRecord.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(scanRecord.length);
            for (byte byteChar : scanRecord)
                stringBuilder.append(String.format("%02X ", byteChar));
            Timber.i("Record Length  : " + String.valueOf(scanRecord.length));
            Timber.i("ScanRecord     : " + stringBuilder.toString());
        }

        if (scanRecord == null || scanRecord.length == 0) {
            Timber.w("No data retrieved!");
            return;
        }

        int ScanRecordIndex = 0;
        String deviceAdress = device.getAddress();

        String scanRecordName = "";
        String deviceUUID = "";

        while (ScanRecordIndex < scanRecord.length) {
            int TripletLength = scanRecord[ScanRecordIndex++];
            if (TripletLength == 0) break;
            byte[] TripletType = new byte[]{scanRecord[ScanRecordIndex]};

            if (TripletType[0] == 0x00) {
                break;
            } else if (TripletType[0] >= 0x02 && TripletType[0] <= 0x07) {// 16-128bit UUID
                final StringBuilder ss = new StringBuilder(TripletLength - 1);
                int ServiceIndex = ScanRecordIndex + TripletLength - 1;
                while (ServiceIndex > ScanRecordIndex) {
                    ss.append(String.format("%02X", scanRecord[ServiceIndex]).toLowerCase());
                    ServiceIndex--;
                }

                deviceUUID = ss.toString();
                Timber.i("DeviceUUID     : " + deviceUUID);

            } else if (TripletType[0] >= 0x08 && TripletType[0] <= 0x09) {// short/complete Localname
                final StringBuilder ss = new StringBuilder(TripletLength - 1);
                int NameIndex = ScanRecordIndex + 1;
                while (NameIndex <= (ScanRecordIndex - 1 + TripletLength)) {
                    ss.append(String.format("%c", scanRecord[NameIndex]));
                    NameIndex++;
                }

                scanRecordName = ss.toString();
                Timber.i("scanRecordName : " + scanRecordName);
            }

            ScanRecordIndex += TripletLength;
        }
    }

    private void scanLeDevice() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopLeScan();
            }
        }, SCAN_PERIOD);

        mScanning = true;
        mBluetoothAdapter.startLeScan(this);
    }

    public void stopLeScan() {
        mScanning = false;
        if (mBluetoothAdapter != null)
            mBluetoothAdapter.stopLeScan(BluetoothLeScanService.this);
        broadcastStopScanUpdate();
    }
}
