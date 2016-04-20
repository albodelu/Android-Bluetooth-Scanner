package com.kodelabs.boilerplate.presentation.ui.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.kodelabs.boilerplate.R;
import com.kodelabs.boilerplate.bluetooth.BluetoothLeScanService;
import com.kodelabs.boilerplate.domain.executor.impl.ThreadExecutor;
import com.kodelabs.boilerplate.presentation.model.BleDevice;
import com.kodelabs.boilerplate.presentation.presenters.ScanPresenter;
import com.kodelabs.boilerplate.presentation.presenters.ScanPresenter.View;
import com.kodelabs.boilerplate.presentation.presenters.impl.ScanPresenterImpl;
import com.kodelabs.boilerplate.presentation.ui.adapters.BleDeviceListAdapter;
import com.kodelabs.boilerplate.threading.MainThreadImpl;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

public class ScanningActivity extends AppCompatActivity implements View {

    public static final int REQUEST_ENABLE_BT = 1;


    @Bind(R.id.titleTextView)
    TextView mTitle;

    @Bind(R.id.deviceList)
    RecyclerView mDevicesList;

    private ScanPresenter mScanPresenter;

    private boolean mBoundService;
    private boolean mRegisteredReceiver;

    private boolean mShowProgress;

    private Messenger mService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
            mBoundService = true;
            mScanPresenter.onServiceConnected();
            Timber.d("Scan service connected!");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBoundService = false;
            Timber.i("Scan service disconnected!");
        }
    };

    private BleDeviceListAdapter mDeviceListAdapter;

    private final BroadcastReceiver mScanUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            switch (action) {

                // we found a new device!
                case BluetoothLeScanService.ACTION_FOUND_DEVICE:
                    // extract device info
                    String deviceName = intent.getStringExtra(BluetoothLeScanService.EXTRA_DEVICE_NAME);
                    String deviceAddress = intent.getStringExtra(BluetoothLeScanService.EXTRA_DEVICE_ADDRESS);
                    int rssi = intent.getIntExtra(BluetoothLeScanService.EXTRA_DEVICE_RSSI, -1000);

                    // convert it to the view model and notify our presenter
                    BleDevice device = new BleDevice(deviceName, deviceAddress, rssi);
                    mScanPresenter.onScanUpdate(device);
                    break;

                case BluetoothLeScanService.ACTION_STOP_SCAN:
                    mScanPresenter.onStopScan();
                    break;

                default:
                    break;
            }
        }
    };

    private void registerScanUpdateReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothLeScanService.ACTION_FOUND_DEVICE);
        filter.addAction(BluetoothLeScanService.ACTION_STOP_SCAN);
        registerReceiver(mScanUpdateReceiver, filter);

        mRegisteredReceiver = true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanning);
        ButterKnife.bind(this);

        Timber.w("CREATE");

        mDevicesList.setLayoutManager(new LinearLayoutManager(this));

        // instantiate the presenter
        mScanPresenter = new ScanPresenterImpl(
                ThreadExecutor.getInstance(),
                MainThreadImpl.getInstance(),
                this
        );

        // register a broadcast receiver for scanning events
        registerScanUpdateReceiver();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.scan_menu, menu);
        if (mShowProgress) {
            MenuItem item = menu.findItem(R.id.action_scan_devices);
            item.setActionView(R.layout.actionbar_indeterminate_progress);
            item.setVisible(true);
        } else {
            MenuItem item = menu.findItem(R.id.action_scan_devices);
            item.setActionView(null);
            item.setIcon(R.drawable.ic_scan_devices);
        }

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_scan_devices:
                onClickScan();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Timber.w("DESTROY");
        mScanPresenter.destroy();

        if (mBoundService) unbindService(mServiceConnection);
        if (mRegisteredReceiver) unregisterReceiver(mScanUpdateReceiver);
    }

    @Override
    public void onClickScan() {
        // clear the list of found devices
        if (mDeviceListAdapter != null) {
            mDeviceListAdapter.removeAll();
            mDeviceListAdapter.notifyDataSetChanged();
        }

        mScanPresenter.onClickScan();
    }

    @Override
    public boolean checkIfBluetoothEnabled() {
        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return false;
        }

        return true;
    }


    /**
     * If the user enables bluetooth we will get a call to this method. This method should check if bluetooth is enabled
     * manually by the user and start the scanning process as if the scan button is pressed.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_ENABLE_BT) { // use has enabled bluetooth
            onClickScan();
        }
    }

    @Override
    public void startScanningService() {

        // check if the service is already connected
        if (mBoundService && mService != null) {
            mScanPresenter.onServiceConnected();
            return;
        }

        Intent intent = new Intent(this, BluetoothLeScanService.class);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void startScan() {
        Message msg = Message.obtain(null, BluetoothLeScanService.MSG_SCAN_DEVICES, 0, 0);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showDevice(BleDevice device) {
        if (mDeviceListAdapter == null) {
            mDeviceListAdapter = new BleDeviceListAdapter(this);
            mDevicesList.setAdapter(mDeviceListAdapter);
        }

        mDeviceListAdapter.addDevice(device);
    }

    @Override
    public void showProgress() {
        mShowProgress = true;
        invalidateOptionsMenu();

        mTitle.setText(getString(R.string.scanning));
    }

    @Override
    public void hideProgress() {
        mShowProgress = false;
        invalidateOptionsMenu();

        mTitle.setText(getString(R.string.press_to_scan));
    }

    @Override
    public void showError(String message) {

    }

}
