package com.vcore.fake.service.context.providers;

import android.os.IInterface;

/**
 * Interface for ContentProvider wrapper implementations that can wrap a real ContentProvider proxy with virtual environment package name substitution.
 */
public interface BContentProvider {
    IInterface wrapper(final IInterface contentProviderProxy, final String appPkg);
}
