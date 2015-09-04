package de.tu.darmstadt.seemoo.ansian.model;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.tu.darmstadt.seemoo.ansian.control.SourceControl;
import de.tu.darmstadt.seemoo.ansian.control.events.ScanAreaUpdateEvent;
import de.tu.darmstadt.seemoo.ansian.control.threads.SourceControlThread;

/**
 * 
 * ScannerBuffer holds all the frequency spectrum data for wider band range
 * scanning. The single data packets are referenced to by their center
 * frequency. ScannerBuffer only takes care of the data holding, the frequency
 * change is handled by {@link SourceControlThread}.
 * 
 * @author Steffen Kreis
 *
 */
public class ScannerBuffer {
	private Hashtable<Long, FFTSample> samples = new Hashtable<Long, FFTSample>();
	private String LOGTAG = "ScannerBuffer";
	private final int DEFAULT_MAXSIZE = 500;
	private int maxSize;
	private long lowerCenterFrequency = Long.MAX_VALUE;
	private long upperCenterFrequency = Long.MIN_VALUE;
	private static long samplerate = 2000000;

	public ScannerBuffer(int size) {
		this.maxSize = size;
		EventBus.getDefault().register(this);
	}

	public ScannerBuffer() {
		new ScannerBuffer(DEFAULT_MAXSIZE);
	}

	/**
	 * 
	 * Adds a sample to the Hashtable. As the frequency is calculated to save as
	 * few as possible fft data packets and minimize the frequency hops, old
	 * data is overwritten automatically
	 * 
	 * @param sample
	 */
	public void addSample(FFTSample sample) {
		if (SourceControl.getSource().isTunerSettled()) {
			long frequency = sample.getCenterFrequency();
			if ((frequency - samplerate / 2) % samplerate == 0) {
				if (frequency > upperCenterFrequency) {
					upperCenterFrequency = frequency;
					long lowestPossibleFrequency = frequency - maxSize * samplerate;
					if (lowerCenterFrequency < lowestPossibleFrequency) {
						lowerCenterFrequency = lowestPossibleFrequency;
						// TODO remove 'lower than possible'-samples
					}
				}
				if (frequency < lowerCenterFrequency) {
					lowerCenterFrequency = frequency;
					long highestPossibleFrequency = frequency + maxSize * samplerate;
					if (upperCenterFrequency > highestPossibleFrequency) {
						upperCenterFrequency = highestPossibleFrequency;
						// TODO remove 'higher than possible'-samples
					}
				}
				// Log.d(LOGTAG, "added Sample " + frequency);
				samples.put(frequency, sample);
			}
		}
	}

	public int getTotalAmount() {
		return (int) ((upperCenterFrequency - lowerCenterFrequency) / samplerate) + 1;
	}

	public FFTSample getScannerSample(long frequency) {
		return samples.get(frequency);
	}

	public Hashtable<Long, FFTSample> getScannerSamples() {
		return samples;
	}

	public long getLowerFrequency() {
		return lowerCenterFrequency;
	}

	public long getUpperFrequency() {
		return upperCenterFrequency;
	}

	public void setSamplerate(long pSamplerate) {
		if (samplerate != pSamplerate) {
			samplerate = pSamplerate;
			samples = new Hashtable<Long, FFTSample>();
		}
	}

	/**
	 * Calculates suitable center frequency. To save unnecessary frequency
	 * changes and also the unwanted redundant data saving, as few as possible
	 * single data spectrums are saved. This is realized by simply separating
	 * the small single spectrums by their bandwidth. The center frequency of a
	 * spectrum references it and the next ones can be found by
	 * adding/subtracting a multiple of the samplerate
	 */
	public static long calcSuitableFrequency(long frequency, boolean isLowerFrequency) {
		frequency = Math.max(frequency, SourceControl.getSource().getMinFrequency());
		frequency = Math.min(frequency, SourceControl.getSource().getMaxFrequency());
		long distance = frequency % samplerate;
		if (!isLowerFrequency && distance == 0) {
			return frequency - samplerate / 2;
		}
		return frequency - distance + samplerate / 2;
	}

	/**
	 * Remove samples which are outside the desired area
	 * 
	 */
	public void removeOuterSamples() {
		Enumeration<Long> samplEnums = samples.keys();
		while (samplEnums.hasMoreElements()) {
			long centerFreq = samplEnums.nextElement();
			if (centerFreq < lowerCenterFrequency || centerFreq > upperCenterFrequency) {
				samples.remove(centerFreq);
			}
		}
	}

	public FFTDrawData getDrawData(int pixelWidth) {
		return getDrawData(samples, pixelWidth);
	}

	/**
	 * handle a change of the desired total bandwidth (fit the lower and upper
	 * frequency accordingly, remove samples which are outside the desired
	 * bandwidth)
	 * 
	 * @param event
	 */
	@Subscribe
	public void onEvent(ScanAreaUpdateEvent event) {
		// Log.d(LOGTAG, "scanareaupdate");
		lowerCenterFrequency = calcSuitableFrequency(event.getLowerFrequency(), true);
		upperCenterFrequency = calcSuitableFrequency(event.getUpperFrequency(), false);
		samplerate = event.getSamplerate() / event.getScanCropDataFactor();
		removeOuterSamples();
	}

/**
 * 
 * Returns the draw data, already scaled to the desired size (screen width)
 * 
 * 
 * @param lastHash
 * @param pixelWidth
 * @return
 */
	public FFTDrawData getDrawData(Hashtable<Long, FFTSample> lastHash, int pixelWidth) {


		Enumeration<Long> enumer = lastHash.keys();
		int startX = pixelWidth - 1;
		int endX = 0;
		float[] mags = new float[pixelWidth];
		for (int pos = 0; pos < mags.length; pos++) {
			mags[pos] = Float.NaN;
		}
		while (enumer.hasMoreElements() && mags.length == pixelWidth) {
			FFTSample fftSample = lastHash.get(enumer.nextElement());
			// Log.d(LOGTAG, "samplefreq: " + fftSample.getCenterFrequency());
			FFTDrawData curDrawData = fftSample.getDrawData(pixelWidth);
			float[] curMags = curDrawData.getValues();
			if (curMags != null) {
				int curStartX = curDrawData.getStart();
				int curEndX = curStartX + curMags.length;
				if (curStartX < startX) {
					startX = curStartX;
				}
				if (curEndX > endX) {
					endX = curEndX;
				}
				int curPos = 0;
				for (int resultPos = curStartX; resultPos < curEndX; resultPos++) {
					try {
						mags[resultPos] = curMags[curPos++];
					} catch (ArrayIndexOutOfBoundsException e) {
						// occurs when orientation changed
						// TODO find satisfying solution
						return null;
					}
				}
			}
		}
		if (startX < endX) {
			mags = Arrays.copyOfRange(mags, startX, endX);
			return new FFTDrawData(mags, startX);
		}
		return null;

	}

}
