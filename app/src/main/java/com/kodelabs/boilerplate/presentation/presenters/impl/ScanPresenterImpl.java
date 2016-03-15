package com.kodelabs.boilerplate.presentation.presenters.impl;

import com.kodelabs.boilerplate.domain.executor.Executor;
import com.kodelabs.boilerplate.domain.executor.MainThread;
import com.kodelabs.boilerplate.domain.interactors.ScanningInteractor;
import com.kodelabs.boilerplate.presentation.model.BleDevice;
import com.kodelabs.boilerplate.presentation.presenters.ScanPresenter;
import com.kodelabs.boilerplate.presentation.presenters.base.AbstractPresenter;

/**
 * Created by dmilicic on 12/13/15.
 */
public class ScanPresenterImpl extends AbstractPresenter implements ScanPresenter,
        ScanningInteractor.Callback {

    private ScanPresenter.View mScanningView;

    public ScanPresenterImpl(Executor executor,
                             MainThread mainThread,
                             View scanningView) {
        super(executor, mainThread);
        mScanningView = scanningView;
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void onError(String message) {

    }

    @Override
    public void onScanUpdate(BleDevice device) {
        mScanningView.showDevice(device);
    }

    @Override
    public void onServiceConnected() {
        mScanningView.startScan();
    }

    @Override
    public void onClickScan() {
        boolean enabled = mScanningView.checkIfBluetoothEnabled();

        if (!enabled)
            return;

        // we first need to start the scanning service
        mScanningView.showProgress();
        mScanningView.startScanningService();
    }

    @Override
    public void onStopScan() {
        mScanningView.hideProgress();
    }
}
