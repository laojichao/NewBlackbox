package com.vcore.fake.hook;

import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.FileDescriptor;
import java.util.Map;

import black.android.os.ServiceManager;

/**
 * Abstract base class for hooking Android Binder-based system services.
 *
 * <p>Extends {@link ClassInvocationStub} and implements {@link IBinder} to intercept
 * Binder IPC calls. Subclasses can replace system services in the {@link ServiceManager}
 * cache by calling {@link #replaceSystemService(String)}. All standard {@link IBinder}
 * methods delegate to the original base binder, while {@link #queryLocalInterface}
 * returns the proxy invocation to enable method-level hooking.</p>
 *
 * @see ClassInvocationStub
 * @see MethodHook
 */
public abstract class BinderInvocationStub extends ClassInvocationStub implements IBinder {
    private final IBinder mBaseBinder;

    /**
     * Constructs a new BinderInvocationStub wrapping the given base binder.
     *
     * @param baseBinder the original {@link IBinder} to delegate standard binder operations to
     */
    public BinderInvocationStub(IBinder baseBinder) {
        this.mBaseBinder = baseBinder;
    }

    /**
     * Called after the proxy is created and methods are bound.
     * Subclasses can override this to perform additional method binding.
     */
    @Override
    protected void onBindMethod() { }

    /**
     * Returns the interface descriptor of the underlying binder.
     *
     * @return the interface descriptor string, or {@code null} if unavailable
     * @throws RemoteException if the remote call fails
     */
    @Nullable
    @Override
    public String getInterfaceDescriptor() throws RemoteException {
        return mBaseBinder.getInterfaceDescriptor();
    }

    /**
     * Checks whether the underlying binder is alive by pinging it.
     *
     * @return {@code true} if the binder responds to a ping
     */
    @Override
    public boolean pingBinder() {
        return mBaseBinder.pingBinder();
    }

    /**
     * Checks whether the underlying binder process is still alive.
     *
     * @return {@code true} if the binder is alive
     */
    @Override
    public boolean isBinderAlive() {
        return mBaseBinder.isBinderAlive();
    }

    /**
     * Returns the proxy invocation as a local interface, enabling method-level interception.
     *
     * @param descriptor the interface descriptor string
     * @return the proxy {@link IInterface} that intercepts method calls
     */
    @Nullable
    @Override
    public IInterface queryLocalInterface(@NonNull String descriptor) {
        return (IInterface) getProxyInvocation();
    }

    /**
     * Dumps the state of the underlying binder to the given file descriptor.
     *
     * @param fd   the file descriptor to dump to
     * @param args optional arguments for the dump
     * @throws RemoteException if the remote call fails
     */
    @Override
    public void dump(@NonNull FileDescriptor fd, @Nullable String[] args) throws RemoteException {
        mBaseBinder.dump(fd, args);
    }

    /**
     * Asynchronously dumps the state of the underlying binder to the given file descriptor.
     *
     * @param fd   the file descriptor to dump to
     * @param args optional arguments for the dump
     * @throws RemoteException if the remote call fails
     */
    @Override
    public void dumpAsync(@NonNull FileDescriptor fd, @Nullable String[] args) throws RemoteException {
        mBaseBinder.dumpAsync(fd, args);
    }

    /**
     * Performs a Binder transaction on the underlying binder.
     *
     * @param code  the transaction code
     * @param data  the parcel containing the transaction data
     * @param reply the parcel to receive the reply, or {@code null}
     * @param flags transaction flags
     * @return {@code true} if the transaction succeeded
     * @throws RemoteException if the remote call fails
     */
    @Override
    public boolean transact(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
        return mBaseBinder.transact(code, data, reply, flags);
    }

    /**
     * Links a death recipient to the underlying binder.
     *
     * @param recipient the {@link DeathRecipient} to notify upon binder death
     * @param flags     flags for the link operation
     * @throws RemoteException if the remote call fails
     */
    @Override
    public void linkToDeath(@NonNull DeathRecipient recipient, int flags) throws RemoteException {
        mBaseBinder.linkToDeath(recipient, flags);
    }

    /**
     * Unlinks a previously linked death recipient from the underlying binder.
     *
     * @param recipient the {@link DeathRecipient} to unlink
     * @param flags     flags for the unlink operation
     * @return {@code true} if the recipient was previously linked
     */
    @Override
    public boolean unlinkToDeath(@NonNull DeathRecipient recipient, int flags) {
        return mBaseBinder.unlinkToDeath(recipient, flags);
    }

    /**
     * Replaces a system service entry in the {@link ServiceManager} cache with this stub.
     * This enables interception of all calls to the named service.
     *
     * @param name the name of the system service to replace (e.g., "package", "activity")
     */
    protected void replaceSystemService(String name) {
        Map<String, IBinder> services = ServiceManager.sCache.get();
        services.put(name, this);
    }
}
