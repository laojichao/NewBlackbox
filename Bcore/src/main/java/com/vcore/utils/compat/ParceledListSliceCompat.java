package com.vcore.utils.compat;

import java.util.List;

import black.android.content.pm.ParceledListSlice;

/**
 * Compatibility wrapper for creating {@code ParceledListSlice} instances via reflection.
 * {@code ParceledListSlice} is a framework-internal class used to efficiently parcel large
 * lists across IPC boundaries. This class handles the case where the single-argument
 * constructor may not be available by falling back to the no-arg constructor with manual
 * item appending.
 */
public class ParceledListSliceCompat {
	/**
	 * Creates a {@code ParceledListSlice} containing the given list of items.
	 * First attempts to use the single-argument constructor that accepts the full list.
	 * If that fails, falls back to the no-arg constructor and appends items one by one,
	 * marking the last slice to indicate the list is complete.
	 *
	 * @param list the list of items to wrap in a {@code ParceledListSlice}
	 * @return the created {@code ParceledListSlice} object
	 */
	public static Object create(List<?> list) {
		Object slice = ParceledListSlice._new1.newInstance(list);
		if (slice != null) {
			return slice;
		} else {
			slice = ParceledListSlice._new0.newInstance();
		}

		for (Object item : list) {
			ParceledListSlice.append.call(slice, item);
		}
		ParceledListSlice.setLastSlice.call(slice, true);
		return slice;
	}
}
