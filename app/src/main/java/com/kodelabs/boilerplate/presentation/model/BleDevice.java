package com.kodelabs.boilerplate.presentation.model;

/**
 * Created by dmilicic on 3/15/16.
 */
public class BleDevice {
    /**
     * The name of the device, it should contain VIFIT for the Medisana gadget.
     */
    private String mName;

    /**
     * The Bluetooth address of the device. We use this address for connecting and interacting with the device.
     */
    private String mAddress;

    /**
     * A device consists of a name and a Bluetooth address
     *
     * @param name    Human readable name of the device.
     * @param address Bluetooth address of the device.
     */
    public BleDevice(String name, String address) {
        mName = name;
        mAddress = address;
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
}
