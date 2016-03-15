package com.kodelabs.boilerplate.presentation.presenters;

import com.kodelabs.boilerplate.presentation.model.BleDevice;
import com.kodelabs.boilerplate.presentation.presenters.base.BasePresenter;
import com.kodelabs.boilerplate.presentation.ui.BaseView;


public interface ScanPresenter extends BasePresenter {

    interface View extends BaseView {

        /**
         * This is called when the user clicks a scan button. This method is the starting point of the scanning procedure.
         */
        void onClickScan();

        /**
         * This method checks if Bluetooth is enabled on the device. If not, it opens an interface to enable bluetooth
         * and reports the result in OnActivityResult method.
         *
         * @return Returns true if bluetooth is already enabled, false otherwise.
         */
        boolean checkIfBluetoothEnabled();

        /**
         * This method checks if the appropriate scanning service is available. If the service is unavailable (unbound/not connected)
         * then it attempts to bind to it (connect to it). Also, reports to the appropriate presenter if the service is connected.
         */
        void startScanningService();

        /**
         * Send a request/message to the scanning service that it should start the actual scanning for BLE devices.
         */
        void startScan();

        /**
         * We have received a new device from the scanning service, show it on the UI in this method.
         *
         * @param device The device to be shown.
         */
        void showDevice(BleDevice device);
    }

    /**
     * We have received a freshly scanned device. This method should tell the UI to display it.
     *
     * @param device The device to be shown.
     */
    void onScanUpdate(BleDevice device);

    /**
     * A specific service used for a specific interactor is now connected. This method should make sure the correct
     * interactor is notified.
     */
    void onServiceConnected();

    /**
     * This should be called when the user sends a scan request through the UI.
     */
    void onClickScan();

    /**
     * This method is should be called when the scanning is no longer active.
     */
    void onStopScan();

}
