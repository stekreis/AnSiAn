package de.tu.darmstadt.seemoo.ansian.control.threads;

import java.util.concurrent.ArrayBlockingQueue;

import android.util.Log;
import android.widget.Toast;
import de.tu.darmstadt.seemoo.ansian.control.SourceControl;
import de.tu.darmstadt.seemoo.ansian.control.StateHandler;
import de.tu.darmstadt.seemoo.ansian.gui.misc.MyToast;
import de.tu.darmstadt.seemoo.ansian.model.AudioSink;
import de.tu.darmstadt.seemoo.ansian.model.SamplePacket;
import de.tu.darmstadt.seemoo.ansian.model.demodulation.Demodulation;
import de.tu.darmstadt.seemoo.ansian.model.demodulation.Demodulation.DemoType;
import de.tu.darmstadt.seemoo.ansian.model.filter.FirFilter;
import de.tu.darmstadt.seemoo.ansian.model.preferences.MiscPreferences;
import de.tu.darmstadt.seemoo.ansian.model.preferences.Preferences;

/**
 * <h1>AnSiAn - Demodulator</h1>
 *
 * Module: Demodulator.java Description: This class implements demodulation of
 * various analog radio modes (FM, AM, SSB). It runs as a separate thread. It
 * will read raw complex samples from a queue, process them (channel selection,
 * filtering, demodulating) and forward the to an AudioSink thread.
 *
 * @author Dennis Mantz
 * @author Markus Grau
 * @author Steffen Kreis
 *
 *         Copyright (C) 2014 Dennis Mantz License:
 *         http://www.gnu.org/licenses/gpl.html GPL version 2 or higher
 *
 *         This library is free software; you can redistribute it and/or modify
 *         it under the terms of the GNU General Public License as published by
 *         the Free Software Foundation; either version 2 of the License, or (at
 *         your option) any later version.
 *
 *         This library is distributed in the hope that it will be useful, but
 *         WITHOUT ANY WARRANTY; without even the implied warranty of
 *         MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *         General Public License for more details.
 *
 *         You should have received a copy of the GNU General Public License
 *         along with this library; if not, write to the Free Software
 *         Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 *         02110-1301 USA
 */
public class Demodulator extends Thread {

	private static Demodulation demodulation;

	private boolean stopRequested = true;

	private static final String LOGTAG = "Demodulator";
	public static final int AUDIO_RATE = 31250; // Even though this is not a
												// proper audio rate, the
												// Android system can
												// handle it properly and it
												// is a integer fraction of
												// the input rate (1MHz).

	public static final int INPUT_RATE = 1000000; // Expected rate of the
													// incoming samples

	// DECIMATION
	private static Decimator decimator; // will do INPUT_RATE -->
										// QUADRATURE_RATE

	// FILTERING (This is the channel filter controlled by the user)
	private static final int USER_FILTER_ATTENUATION = 20;
	private FirFilter userFilter = null;

	private SamplePacket quadratureSamples;

	// AUDIO OUTPUT
	private AudioSink audioSink = null; // Will do QUADRATURE_RATE -->

	private MiscPreferences preferences;

	// AUDIO_RATE and audio output
	public static Demodulation getDemodulation() {
		return demodulation;
	}

	/**
	 * Constructor. Creates a new demodulator block reading its samples from the
	 * given input queue and returning the buffers to the given output queue.
	 * Expects input samples to be at baseband (mixing is done by the scheduler)
	 *
	 * @param inputQueue
	 *            Queue that delivers received baseband signals
	 * @param outputQueue
	 *            Queue to return used buffers from the inputQueue
	 * @param packetSize
	 *            Size of the packets in the input queue
	 * @param ansianService
	 */
	public Demodulator(ArrayBlockingQueue<SamplePacket> inputQueue, int packetSize, DemoType type) {
		preferences = Preferences.MISC_PREFERENCE;
		demodulation = Demodulation.getDemodulation(type);

		// Create internal sample buffers:
		// Note that we create the buffers for the case that there is no
		// downsampling necessary
		// All other cases with input decimation > 1 are also possible because
		// they only need
		// smaller buffers.
		this.quadratureSamples = new SamplePacket(packetSize);

		// Create Audio Sink
		this.audioSink = new AudioSink(packetSize, AUDIO_RATE);

		// Create Decimator block
		// Note that the decimator directly reads from the inputQueue and also
		// returns processed packets to the
		// output queue.
		decimator = new Decimator(demodulation.getQuadratureRate(), packetSize, inputQueue);

	}

	/**
	 * Will set the cut off frequency of the user filter
	 * 
	 * @param channelWidth
	 *            channel width (single side) in Hz
	 * @return true if channel width is valid, false if out of range
	 */
	public boolean setChannelWidth(int channelWidth) {
		if (channelWidth < demodulation.getMinUserFilterWidth() || channelWidth > demodulation.getMaxUserFilterWidth())
			return false;
		demodulation.setUserFilterCutOff(channelWidth);
		userFilter = null;
		return true;
	}

	/**
	 * @return Current width (cut-off frequency - one sided) of the user filter
	 */
	public int getChannelWidth() {
		return demodulation.getUserFilterCutOff();
	}

	/**
	 * Starts the thread. This thread will start 2 more threads for decimation
	 * and audio output. These threads are managed by the Demodulator and
	 * terminated, when the Demodulator thread terminates.
	 */
	@Override
	public synchronized void start() {
		stopRequested = false;

		super.start();
	}

	/**
	 * Stops the thread
	 */
	public void stopDemodulator() {
		stopRequested = true;
	}

	@Override
	public void run() {
		SamplePacket inputSamples = null;
		SamplePacket audioBuffer = null;

		Log.i(LOGTAG, "Demodulator started. (Thread: " + this.getName() + ")");

		// Start the audio sink thread:
		audioSink.start();

		// Start decimator thread:
		decimator.start();

		while (!stopRequested) {

			// Get downsampled packet from the decimator:
			inputSamples = decimator.getDecimatedPacket(1000);

			// Verify the input sample packet is not null:
			if (inputSamples == null) {
				// Log.d(LOGTAG,
				// "run: Decimated sample is null. skip this round...");
				continue;
			}

			// filtering [sample rate is QUADRATURE_RATE]
			applyUserFilter(inputSamples, quadratureSamples); // The result from
																// filtering is
																// stored in
																// quadratureSamples

			// get buffer from audio sink
			// TODO why was it 1000 before? maybe set back
			audioBuffer = new SamplePacket(quadratureSamples.getRe().length);
			// audioBuffer = new SamplePacket(1000);

			// demodulate [sample rate is QUADRATURE_RATE]
			demodulation.demodulate(quadratureSamples, audioBuffer);

			// play audio [sample rate is QUADRATURE_RATE]
			audioSink.enqueuePacket(audioBuffer);

		}

		// Stop the audio sink thread:
		audioSink.stopSink();

		// Stop the decimator thread:
		decimator.stopDecimator();

		Log.i(LOGTAG, "Demodulator stopped. (Thread: " + this.getName() + ")");
	}

	/**
	 * Will filter the samples in input according to the user filter settings.
	 * Filtered samples are stored in output. Note: All samples in output will
	 * always be overwritten!
	 *
	 * @param input
	 *            incoming (unfiltered) samples
	 * @param output
	 *            outgoing (filtered) samples
	 */
	private void applyUserFilter(SamplePacket input, SamplePacket output) {
		// Verify that the filter is still correct configured:
		if (userFilter == null || ((int) userFilter.getCutOffFrequency()) != demodulation.getUserFilterCutOff()) {
			// We have to (re-)create the user filter:
			userFilter = FirFilter.createLowPass(1, 1, input.getSampleRate(), demodulation.getUserFilterCutOff(),
					input.getSampleRate() * 0.10f, USER_FILTER_ATTENUATION);
			if (userFilter == null)
				return; // This may happen if input samples changed rate or
						// demodulation was turned off. Just skip the filtering.
			Log.d(LOGTAG,
					"applyUserFilter: created new user filter with " + userFilter.getNumberOfTaps()
							+ " taps. Decimation=" + userFilter.getDecimation() + " Cut-Off="
							+ userFilter.getCutOffFrequency() + " transition=" + userFilter.getTransitionWidth());
		}
		output.setSize(0); // mark buffer as empty
		if (userFilter != null && userFilter.filter(input, output, 0, input.size()) < input.size()) {
			Log.e(LOGTAG, "applyUserFilter: could not filter all samples from input packet.");
		}
	}

	/**
	 * Will set the modulation mode to the given value. Takes care of adjusting
	 * the scheduler and the demodulator respectively and updates the action bar
	 * menu item.
	 *
	 * @param demod
	 *            Demodulator.DEMODULATION_OFF, *_AM, *_NFM, *_WFM
	 */
	public void setDemodulationMode(DemoType type) {
		if (demodulation.getType() == type)
			return;

		// if (demodulation.getType()==DemoType.MORSE)
		// EventBus.getDefault().unregister(demodulation);

		// set demodulation mode in demodulator:
		demodulation = Demodulation.getDemodulation(type);
		decimator.setOutputSampleRate(demodulation.getQuadratureRate());
		userFilter = null;

		if (type != DemoType.OFF) {

			// adjust sample rate of the source:
			SourceControl.getSource().setSampleRate(Demodulator.INPUT_RATE);
			preferences.setSampleRate(INPUT_RATE);

			// Verify that the source supports the sample rate:
			if (SourceControl.getSource().getSampleRate() != Demodulator.INPUT_RATE) {
				Log.e(LOGTAG, "setDemodulationMode: cannot adjust source sample rate!");
				MyToast.makeText("Source does not support the sample rate necessary for demodulation ("
						+ INPUT_RATE / 1000000 + " Msps)", Toast.LENGTH_LONG);

				StateHandler.setDemodulationMode(DemoType.OFF);

			}
		}
	}

}
