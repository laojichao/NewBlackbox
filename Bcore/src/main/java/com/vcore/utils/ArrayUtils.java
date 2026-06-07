package com.vcore.utils;

import java.util.Arrays;

/**
 * Utility class providing common array manipulation operations including type-based searching,
 * resizing, emptiness checks, and primitive array conversions.
 */
public class ArrayUtils {
	/**
	 * Trims an array to the specified size by creating a copy with only the first {@code size} elements.
	 *
	 * @param <T>   the component type of the array
	 * @param array the source array to trim; may be {@code null}
	 * @param size  the desired number of elements in the returned array
	 * @return a new array of length {@code size} containing the first {@code size} elements,
	 *         the original array if its length already equals {@code size}, or {@code null}
	 *         if the input array is {@code null} or {@code size} is 0
	 */
	public static<T> T[] trimToSize(T[] array, int size) {
		if (array == null || size == 0) {
			return null;
		} else if (array.length == size) {
			return array;
		}
		return Arrays.copyOf(array, size);
	}

	/**
	 * Finds the index of the first element in the array whose runtime class matches
	 * the given type exactly (not using instanceof).
	 *
	 * @param array the array to search; may be {@code null}
	 * @param type  the exact class to match against element runtime types
	 * @return the zero-based index of the first matching element, or {@code -1} if not found
	 *         or the array is empty
	 */
	public static int indexOfFirst(Object[] array, Class<?> type) {
		if (!isEmpty(array)) {
			int N = -1;
			for (Object one : array) {
				N++;
				if (one != null && type == one.getClass()) {
					return N;
				}
			}
		}
		return -1;
	}

	/**
	 * Finds the index of the first element in the array (starting from {@code sequence})
	 * that is an instance of the given type (using {@code instanceof} semantics).
	 *
	 * @param array    the array to search; may be {@code null}
	 * @param type     the class to test against using {@code instanceof}
	 * @param sequence the starting index to begin the search from
	 * @return the zero-based index of the first matching element, or {@code -1} if not found
	 */
	public static int indexOfObject(Object[] array, Class<?> type, int sequence) {
		if (array == null) {
			return -1;
		}

		while (sequence < array.length) {
			if (type.isInstance(array[sequence])) {
				return sequence;
			}
			sequence++;
		}
		return -1;
	}

	/**
	 * Finds the index of the last element in the array whose runtime class matches
	 * the given type exactly (not using instanceof).
	 *
	 * @param array the array to search; may be {@code null}
	 * @param type  the exact class to match against element runtime types
	 * @return the zero-based index of the last matching element, or {@code -1} if not found
	 *         or the array is empty
	 */
	public static int indexOfLast(Object[] array, Class<?> type) {
		if (!isEmpty(array)) {
			for (int N = array.length; N > 0; N--) {
				Object one = array[N - 1];
				if (one != null && one.getClass() == type) {
					return N - 1;
				}
			}
		}
		return -1;
	}

	/**
	 * Converts an {@code Integer[]} array to a primitive {@code int[]} array via auto-unboxing.
	 *
	 * @param array the {@code Integer} array to convert; must not be {@code null}
	 * @return a primitive {@code int[]} with the same elements
	 */
	public static int[] toInt(Integer[] array) {
		int[] newArray = new int[array.length];

		for (int i = 0; i < array.length; i++) {
			newArray[i] = array[i];
		}
		return newArray;
	}

	/**
	 * Checks whether the given array is {@code null} or has zero length.
	 *
	 * @param <T>   the component type of the array
	 * @param array the array to check
	 * @return {@code true} if the array is {@code null} or empty
	 */
	public static <T> boolean isEmpty(T[] array) {
		return array == null || array.length == 0;
	}
}
