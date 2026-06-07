package android.os;

import android.annotation.TargetApi;

import java.io.IOException;

/**
 * Wrapper class that offers to transport typical {@link Throwable} across a
 * {@link Binder} call. This class is typically used to transport exceptions
 * that cannot be modified to add {@link Parcelable} behavior, such as
 * {@link IOException}.
 * <ul>
 * <li>The wrapped throwable must be defined as system class (that is, it must
 * be in the same {@link ClassLoader} as {@link Parcelable}).
 * <li>The wrapped throwable must support the
 * {@link Throwable#Throwable(String)} constructor.
 * <li>The receiver side must catch any thrown {@link ParcelableException} and
 * call {@link #maybeRethrow(Class)} for all expected exception types.
 * </ul>
 *
 * @see Parcelable
 * @see Binder
 */
@TargetApi(Build.VERSION_CODES.O)
public final class ParcelableException extends RuntimeException implements Parcelable {
    /**
     * Constructs a new {@link ParcelableException} wrapping the given throwable.
     *
     * @param t the {@link Throwable} to wrap
     */
    public ParcelableException(Throwable t) {
        super(t);
    }

    /**
     * Re-throws the wrapped exception if it is an instance of the specified class.
     * This method should be called on the receiving end of a Binder call to properly
     * propagate the original exception type.
     *
     * @param <T>   the expected exception type
     * @param clazz the {@link Class} of the exception to re-throw
     * @throws T if the wrapped exception is an instance of the specified class
     */
    public <T extends Throwable> void maybeRethrow(Class<T> clazz) throws T {
        throw new RuntimeException("Stub!");
    }

    /**
     * Reads a throwable from the given {@link Parcel}.
     *
     * @param in the {@link Parcel} to read from
     * @return the deserialized {@link Throwable}
     */
    public static Throwable readFromParcel(Parcel in) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Writes the given throwable to the given {@link Parcel}.
     *
     * @param out the {@link Parcel} to write to
     * @param t   the {@link Throwable} to serialize
     */
    public static void writeToParcel(Parcel out, Throwable t) {
        throw new RuntimeException("Stub!");
    }

    /**
     * {@inheritDoc}
     *
     * @return 0, as this object contains no special objects
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Writes this exception to the given {@link Parcel}.
     *
     * @param dest  the {@link Parcel} to write to
     * @param flags additional flags about how the object should be written
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Parcelable creator for {@link ParcelableException} instances.
     */
    public static final Creator<ParcelableException> CREATOR = new Creator<ParcelableException>() {
        @Override
        public ParcelableException createFromParcel(Parcel source) {
            return new ParcelableException(readFromParcel(source));
        }

        @Override
        public ParcelableException[] newArray(int size) {
            return new ParcelableException[size];
        }
    };
}
