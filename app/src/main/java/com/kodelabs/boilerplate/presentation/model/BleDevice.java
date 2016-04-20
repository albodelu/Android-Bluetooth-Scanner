package com.kodelabs.boilerplate.presentation.model;

public class BleDevice {
    /**
     * The name of the device.
     */
    private String mName;

    /**
     * The Bluetooth address of the device. We use this address for connecting and interacting with the device.
     */
    private String mAddress;

    private int mRssi;

    /**
     * A device consists of a name and a Bluetooth address
     *
     * @param name    Human readable name of the device.
     * @param address Bluetooth address of the device.
     * @param rssi    Signal strength.
     */
    public BleDevice(String name, String address, int rssi) {
        mName = name;
        mAddress = address;
        mRssi = rssi;
    }


    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    public int getRssi() {
        return mRssi;
    }

    public void setRssi(int rssi) {
        mRssi = rssi;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BleDevice bleDevice = (BleDevice) o;

        if (!mName.equals(bleDevice.mName)) return false;
        return mAddress.equals(bleDevice.mAddress);

    }

    @Override
    public int hashCode() {
        int result = mName.hashCode();
        result = 31 * result + mAddress.hashCode();
        return result;
    }
}
