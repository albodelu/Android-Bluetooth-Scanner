package com.kodelabs.boilerplate.domain.repository;

import com.kodelabs.boilerplate.presentation.model.BleDevice;

/**
 * A sample repository with CRUD operations on a model.
 */
public interface Repository {

    boolean insert(BleDevice model);

    boolean update(BleDevice model);

    BleDevice get(Object id);

    boolean delete(BleDevice model);
}
