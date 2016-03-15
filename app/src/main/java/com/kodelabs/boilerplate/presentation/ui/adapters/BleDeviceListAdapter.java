package com.kodelabs.boilerplate.presentation.ui.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kodelabs.boilerplate.R;
import com.kodelabs.boilerplate.presentation.model.BleDevice;
import com.kodelabs.boilerplate.presentation.ui.adapters.BleDeviceListAdapter.ViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by dmilicic on 7/29/15.
 */
public class BleDeviceListAdapter extends RecyclerView.Adapter<ViewHolder> implements View.OnClickListener {

    private List<BleDevice> mDevices;
    private Context         mContext;


    public static class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.device_name)
        public TextView mDeviceName;

        @Bind(R.id.connect_button)
        public TextView mConnectButton;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    public BleDeviceListAdapter(Context context) {
        mDevices = new ArrayList<>();
        mContext = context;
    }

    public void addDevice(BleDevice device) {
        mDevices.add(device);
    }

    @Override
    public void onClick(View v) {

        RecyclerView parent = (RecyclerView) v.getParent();
        int position = parent.indexOfChild(v);

        // get the device
        final BleDevice device = mDevices.get(position);

        // start building a yes/no dialog to make sure to ask the user if he selected the correct device
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        // set the message of the dialog
        builder.setMessage(mContext.getString(R.string.pair_device_dialog))

                // if the user presses YES then start pairing with the device
                .setPositiveButton(mContext.getString(R.string.yes), new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // create an intent that will start the pairing activity
//                        Intent intent = new Intent(mContext, MainActivity.class);
//                        intent.putExtra(BluetoothLeScanService.EXTRA_DEVICE_NAME, device.getName());
//                        intent.putExtra(BluetoothLeScanService.EXTRA_DEVICE_ADDRESS, device.getAddress());
//                        mContext.startActivity(intent);

                    }

                    // if the user presses no simply hide the dialog
                }).setNegativeButton(mContext.getString(R.string.no), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // nothing to do here
            }
        }).show();


    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.scanned_device_item, parent, false);

        view.setOnClickListener(this);

        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BleDevice device = mDevices.get(position);
        String deviceName = device.getName();
        String deviceAddr = device.getAddress();

        if (deviceName == null)
            deviceName = "Unknown device";
        if (deviceAddr == null)
            deviceAddr = "Unknown address";

        holder.mDeviceName.setText(device.getName());
    }


    @Override
    public int getItemCount() {
        return mDevices.size();
    }

    public void removeAll() {
        if (mDevices == null) return;
        mDevices.clear();
    }
}
