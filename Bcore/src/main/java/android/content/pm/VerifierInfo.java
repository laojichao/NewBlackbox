package android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;

import java.security.PublicKey;

/**
 * Stub implementation of Android's {@code VerifierInfo} class.
 *
 * <p>Contains the package name and public key of a package verifier. Package verifiers
 * are responsible for confirming the integrity and authenticity of APKs before installation.
 * This information is included in the package installation flow to validate that the APK
 * was verified by a trusted source.</p>
 *
 * <p>Implements {@link Parcelable} for efficient transport across Binder IPC boundaries.</p>
 *
 * @see android.content.pm.PackageParser
 */
public class VerifierInfo implements Parcelable {
    /**
     * Parcelable creator for {@link VerifierInfo} instances.
     */
    public static final Parcelable.Creator<VerifierInfo> CREATOR = new Parcelable.Creator<VerifierInfo>() {
        public VerifierInfo createFromParcel(final Parcel source) {
            return new VerifierInfo(source);
        }

        public VerifierInfo[] newArray(final int size) {
            return new VerifierInfo[size];
        }
    };

    /**
     * Constructs a new {@link VerifierInfo} with the given package name and public key.
     *
     * @param packageName the package name of the verifier application
     * @param publicKey   the {@link PublicKey} used to verify APK signatures
     * @throws RuntimeException always, as this is a stub implementation
     */
    public VerifierInfo(final String packageName, final PublicKey publicKey) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Constructs a {@link VerifierInfo} by reading from a {@link Parcel}.
     *
     * @param source the {@link Parcel} to read from
     */
    private VerifierInfo(final Parcel source) {
        throw new RuntimeException("Stub!");
    }

    /**
     * {@inheritDoc}
     *
     * @return a bitmask indicating the set of special objects in this Parcelable
     */
    @Override
    public int describeContents() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Writes this verifier info to the given {@link Parcel}.
     *
     * @param dest  the {@link Parcel} to write to
     * @param flags additional flags about how the object should be written
     */
    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        throw new RuntimeException("Stub!");
    }
}
