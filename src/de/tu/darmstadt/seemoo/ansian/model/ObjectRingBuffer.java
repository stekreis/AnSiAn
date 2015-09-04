package de.tu.darmstadt.seemoo.ansian.model;

import java.lang.reflect.Array;

public class ObjectRingBuffer<T> {

	private T[] array;
	private Class<T> c;
	private int currentIndex = 0;
	private int maxIndex;
	private int size;
	private boolean filled;
	private final static int defaultSize = 500;
	@SuppressWarnings("unused")
	private static final String LOGTAG = "ObjectRingBuffer";

	@SuppressWarnings("unchecked")
	public ObjectRingBuffer(Class<T> c, int size) {
		this.c = c;
		array = (T[]) Array.newInstance(c, size);
		this.size = size;
		maxIndex = size - 1;
		filled = false;
	}

	@SuppressWarnings("unchecked")
	public ObjectRingBuffer(Class<T> c) {
		this(c, defaultSize);
	}

	public void add(T obj) {
		if (currentIndex++ == maxIndex) {
			currentIndex = 0;
			filled = true;
			// Log.d(LOGTAG, "full."+maxIndex);
		}
		if (array != null)
			array[currentIndex] = obj;
	}

	public T getLast() {
		return array[currentIndex];
	}

	public T[] getLast(int length) {
		if (length > size)
			length = size;

		int tempIndex = currentIndex;

		@SuppressWarnings("unchecked")
		T[] result = (T[]) Array.newInstance(c, length);

		// if (length > tempIndex) {
		// System.arraycopy(array, tempIndex, result, 0, maxIndex-tempIndex);
		// System.arraycopy(array, 0, result, maxIndex-tempIndex,
		// length-tempIndex-1);
		// } else
		// System.arraycopy(array, tempIndex - length, result, 0, length-1);
		System.arraycopy(array, 0, result, 0, length);
		return result;
	}

	public T[] getSamples() {
		return getLast(size);
	}

}
