package com.vcore.utils;

import android.os.Parcel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

/**
 * Utility class providing common file and stream operations including reading files to byte arrays,
 * copying files via NIO channels, writing byte data to files, recursively deleting directories,
 * and checking for symbolic links.
 */
public class FileUtils {
    /**
     * Counts the number of direct children in a directory, or returns 1 for a regular file.
     *
     * @param file the file or directory to count
     * @return the number of direct children if the file is a directory, 1 if it is a regular file,
     *         or -1 if the file does not exist
     */
    public static int count(File file) {
        if (!file.exists()) {
            return -1;
        }

        if (file.isFile()) {
            return 1;
        }

        if (file.isDirectory()) {
            String[] fs = file.list();
            return fs == null ? 0 : fs.length;
        }
        return 0;
    }

    /**
     * Renames the original file to the new file.
     *
     * @param origFile the file to rename
     * @param newFile  the new file name/path
     * @return {@code true} if the rename was successful
     */
    public static boolean renameTo(File origFile, File newFile) {
        return origFile.renameTo(newFile);
    }

    /**
     * Reads the entire contents of a file into an Android {@link Parcel}. The parcel's data
     * position is reset to 0 after unmarshalling, making it ready for reading.
     *
     * @param file the file to read
     * @return a {@link Parcel} containing the file's raw bytes, ready for reading from position 0
     * @throws IOException if the file cannot be read
     */
    public static Parcel readToParcel(File file) throws IOException {
        Parcel in = Parcel.obtain();
        byte[] bytes = toByteArray(file);

        in.unmarshall(bytes, 0, bytes.length);
        in.setDataPosition(0);
        return in;
    }

    /**
     * Determines whether the given file is a symbolic link by comparing its canonical and
     * absolute paths. If the canonical path differs from the absolute path, the file is a symlink.
     *
     * @param file the file to check
     * @return {@code true} if the file is a symbolic link
     * @throws IOException          if an I/O error occurs while resolving canonical paths
     * @throws NullPointerException if {@code file} is {@code null}
     */
    public static boolean isSymlink(File file) throws IOException {
        if (file == null) {
            throw new NullPointerException("File must not be null");
        }

        File canon;
        if (file.getParent() == null) {
            canon = file;
        } else {
            File canonDir = file.getParentFile().getCanonicalFile();
            canon = new File(canonDir, file.getName());
        }
        return !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
    }

    /**
     * Writes the marshalled bytes of a {@link Parcel} to a {@link FileOutputStream}.
     *
     * @param p   the parcel to marshal and write
     * @param fos the output stream to write the marshalled data to
     * @throws IOException if an I/O error occurs during writing
     */
    public static void writeParcelToOutput(Parcel p, FileOutputStream fos) throws IOException {
        fos.write(p.marshall());
    }

    /**
     * Reads the entire contents of a file into a byte array.
     *
     * @param file the file to read
     * @return a byte array containing all bytes from the file
     * @throws IOException if the file cannot be read
     */
    public static byte[] toByteArray(File file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        try {
            return toByteArray(fileInputStream);
        } finally {
            closeQuietly(fileInputStream);
        }
    }

    /**
     * Reads all bytes from an {@link InputStream} into a byte array.
     *
     * @param inStream the input stream to read from
     * @return a byte array containing all bytes read from the stream
     * @throws IOException if an I/O error occurs during reading
     */
    public static byte[] toByteArray(InputStream inStream) throws IOException {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc;
        while ((rc = inStream.read(buff, 0, 100)) > 0) {
            swapStream.write(buff, 0, rc);
        }
        return swapStream.toByteArray();
    }

    /**
     * Recursively deletes a directory and all of its contents. Symbolic links are not followed
     * into, so only the link itself is deleted rather than its target.
     *
     * @param dir the directory (or file) to delete
     * @return the total number of files and directories successfully deleted
     */
    public static int deleteDir(File dir) {
        int count = 0;
        if (dir.isDirectory()) {
            boolean link = false;
            try {
                link = isSymlink(dir);
            } catch (Exception ignored) { }

            if (!link) {
                String[] children = dir.list();
                for (String file : children) {
                    count += deleteDir(new File(dir, file));
                }
            }
        }

        if (dir.delete()) {
            count++;
        }
        return count;
    }

    /**
     * Recursively deletes a directory and all of its contents by path string.
     *
     * @param dir the directory path to delete
     * @return the total number of files and directories successfully deleted
     */
    public static int deleteDir(String dir) {
        return deleteDir(new File(dir));
    }

    /**
     * Writes a byte array to a file using NIO channel transfer for efficient I/O.
     *
     * @param data   the byte data to write
     * @param target the target file to write to
     * @throws IOException if an I/O error occurs during writing
     */
    public static void writeToFile(byte[] data, File target) throws IOException {
        try (ReadableByteChannel src = Channels.newChannel(new ByteArrayInputStream(data));
             FileOutputStream fo = new FileOutputStream(target);
             FileChannel out = fo.getChannel()) {
            out.transferFrom(src, 0, data.length);
        }
    }

    /**
     * Copies data from an {@link InputStream} to a target file using a 4KB buffer.
     * Both the input stream and output stream are closed after completion, regardless
     * of whether an exception occurred.
     *
     * @param inputStream the source input stream
     * @param target      the target file to write to
     */
    public static void copyFile(InputStream inputStream, File target) {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(target);
            byte[] data = new byte[4096];
            int len;
            while ((len = inputStream.read(data)) != -1) {
                outputStream.write(data, 0, len);
            }
            outputStream.flush();
        } catch (Throwable e) {
            // Ignore
        } finally {
            closeQuietly(inputStream);
            closeQuietly(outputStream);
        }
    }

    /**
     * Copies a source file to a target file using NIO {@link FileChannel}s with a 1KB
     * {@link ByteBuffer}.
     *
     * @param source the source file to copy from
     * @param target the target file to copy to
     * @throws IOException if an I/O error occurs during copying
     */
    public static void copyFile(File source, File target) throws IOException {
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(source);
            outputStream = new FileOutputStream(target);
            FileChannel iChannel = inputStream.getChannel();
            FileChannel oChannel = outputStream.getChannel();

            ByteBuffer buffer = ByteBuffer.allocate(1024);
            while (true) {
                buffer.clear();
                int r = iChannel.read(buffer);
                if (r == -1) {
                    break;
                }

                buffer.limit(buffer.position());
                buffer.position(0);
                oChannel.write(buffer);
            }
        } finally {
            closeQuietly(inputStream);
            closeQuietly(outputStream);
        }
    }

    /**
     * Closes a {@link Closeable} resource, silently ignoring any exception.
     *
     * @param closeable the resource to close; may be {@code null}
     */
    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) { }
        }
    }

    /**
     * Creates the directory named by this path, including any necessary parent directories,
     * if it does not already exist.
     *
     * @param path the directory to create
     */
    public static void mkdirs(File path) {
        if (!path.exists()) {
            path.mkdirs();
        }
    }

    /**
     * Creates the directory named by this path string, including any necessary parent directories,
     * if it does not already exist.
     *
     * @param path the directory path string to create
     */
    public static void mkdirs(String path) {
        mkdirs(new File(path));
    }

    /**
     * Checks whether a file or directory exists at the given path.
     *
     * @param path the file or directory path to check
     * @return {@code true} if a file or directory exists at the given path
     */
    public static boolean isExist(String path) {
        return new File(path).exists();
    }
}
