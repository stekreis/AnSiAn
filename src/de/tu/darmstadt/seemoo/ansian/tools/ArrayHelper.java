package de.tu.darmstadt.seemoo.ansian.tools;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * 
 * @author Markus Grau and Steffen Kreis
 * 
 *         provides several array operations
 *
 */
public class ArrayHelper {

	private float[] array;
	private float[] result;
	private int center;

	public ArrayHelper(float[] array) {
		this.array = array;
		center = array.length / 2;
	}

	public float[] getScaledValues(int amount) {
		if (amount != 0) {
			return getScaledValues(amount, 1);
		} else {
			return null;
		}
	}

	private float[] getCenterValues(int range) {
		int lowerPos = center - range;
		int upperPos = center + range;
		// if (lowerPos < 0) {
		// lowerPos = 0;
		// }
		// if (upperPos > array.length) {
		// upperPos = array.length;
		// }
		return Arrays.copyOfRange(array, lowerPos, upperPos);
	}

	public float[] getScaledValues(int amount, float scale) {
		return getScaledValues(amount, scale, 1, true);
	}

	public float[] getScaledValues(int amount, float yScale, boolean cut) {
		return getScaledValues(amount, 1, yScale, cut);
	}

	public float[] getScaledValues(int amount, float scale, float yScale, boolean cut) {
		float[] tempResult = new float[amount]; // new
		if (cut) {
			if (scale != 1) {
				result = getCenterValues((int) ((array.length / scale) / 2));
			} else {
				result = Arrays.copyOf(array, array.length);
				scale *= (float) array.length / amount; // new
			}
		} else {
			result = Arrays.copyOf(array, array.length);
			scale = (float) array.length / amount; // new
			// scale = result.length/amount;
		}
		float max = 0;
		for (int i = 0; i < amount; i++) {
			tempResult[i] = calcAverage(result, Math.round(max), Math.round(max += scale)) * yScale;
		}
		return result = tempResult;
	}

	public float getAverage(int range) {
		float[] temp = Arrays.copyOfRange(array, center - range, center + range);
		return calcAverage(temp);
	}

	private float calcAverage(float[] temp) {
		return calcAverage(temp, 0, temp.length);
	}

	private float calcAverage(float[] values, int from, int till) {
		float avg = 0;
		if (from == till) {
			return values[from];
		}
		for (int pos = from; pos < till; pos++) {
			avg += values[pos];
		}
		return avg / (till - from);
	}

	public float getAverage() {
		return calcAverage(array);
	}

	/**
	 * @author Steffen Kreis
	 * 
	 *         concatenates two generic arrays of same data type. Does not
	 *         support primitive data types
	 * @param first
	 *            array
	 * @param second
	 * @return concatenated array
	 */
	public static <T> T[] concatenate(T[] first, T[] second) {
		int firstLength = first.length;
		int secondLength = second.length;

		@SuppressWarnings("unchecked")
		T[] res = (T[]) Array.newInstance(first.getClass().getComponentType(), firstLength + secondLength);
		System.arraycopy(first, 0, res, 0, firstLength);
		System.arraycopy(second, 0, res, firstLength, secondLength);

		return res;
	}

	/**
	 * @author Steffen Kreis
	 * 
	 *         concatenates two float arrays. See
	 *         {@link #concatenate(Object[], Object[]) concatenate}. The generic
	 *         method does not support primitive datatypes, for float use this
	 *         one instead.
	 * 
	 * @param first
	 *            array
	 * @param second
	 * @return concatenated array
	 */
	public static float[] concatenate(float[] first, float[] second) {
		int firstLength = first.length;
		int secondLength = second.length;
		float[] res = new float[firstLength + secondLength];
		System.arraycopy(first, 0, res, 0, firstLength);
		System.arraycopy(second, 0, res, firstLength, secondLength);
		return res;
	}
}
