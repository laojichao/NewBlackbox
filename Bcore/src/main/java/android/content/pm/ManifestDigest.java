package android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.jar.Attributes;

/**
 * Stub implementation of Android's {@code ManifestDigest} class.
 *
 * <p>Represents a cryptographic digest of an APK's {@code AndroidManifest.xml} file.
 * This digest is used to verify the integrity and identity of a package's manifest
 * content, enabling comparison of whether two package versions have the same manifest.</p>
 *
 * <p>Implements {@link Parcelable} for efficient transport across Binder IPC boundaries.</p>
 *
 * @see PackageParser.Package#manifestDigest
 */
public class ManifestDigest implements Parcelable {
    /**
     * Constructs a new {@code ManifestDigest} from the given raw digest bytes.
     *
     * @param digest the SHA-256 (or similar) hash bytes of the manifest
     */
    ManifestDigest(final byte[] digest) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Constructs a {@code ManifestDigest} by reading from a {@link Parcel}.
     *
     * @param source the {@link Parcel} to read from
     */
    private ManifestDigest(final Parcel source) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Creates a {@code ManifestDigest} from the given JAR manifest {@link Attributes}.
     *
     * @param attributes the {@link Attributes} from a JAR manifest to compute the digest from
     * @return a new {@code ManifestDigest} instance
     */
    static ManifestDigest fromAttributes(final Attributes attributes) {
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
     * Compares this digest to another object for equality.
     *
     * @param o the object to compare with
     * @return {@code true} if the other object is a {@code ManifestDigest} with
     *         the same digest value; {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Returns a hash code for this digest.
     *
     * @return the hash code value based on the digest bytes
     */
    @Override
    public int hashCode() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Returns a human-readable string representation of this digest.
     *
     * @return a string representation of the digest
     */
    @Override
    public String toString() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Writes this digest to the given {@link Parcel}.
     *
     * @param dest  the {@link Parcel} to write to
     * @param flags additional flags about how the object should be written
     */
    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Parcelable creator for {@link ManifestDigest} instances.
     */
    public static final Parcelable.Creator<ManifestDigest> CREATOR = new Parcelable.Creator<ManifestDigest>() {
        public ManifestDigest createFromParcel(Parcel source) {
            return new ManifestDigest(source);
        }

        public ManifestDigest[] newArray(int size) {
            return new ManifestDigest[size];
        }
    };
}
