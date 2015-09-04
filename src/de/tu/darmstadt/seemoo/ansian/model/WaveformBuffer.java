package de.tu.darmstadt.seemoo.ansian.model;

import java.util.Arrays;

import de.tu.darmstadt.seemoo.ansian.control.StateHandler;

public class WaveformBuffer {

	private ObjectRingBuffer<SamplePacket> origPackets;
	private ObjectRingBuffer<WaveformDrawData> drawPackets;

	public WaveformBuffer() {
		origPackets = new ObjectRingBuffer<SamplePacket>(SamplePacket.class, 300);
		drawPackets = new ObjectRingBuffer<WaveformDrawData>(WaveformDrawData.class, 20);
	}

	public void clearDrawData() {
		drawPackets = new ObjectRingBuffer<WaveformDrawData>(WaveformDrawData.class, 10);
	}

	public void addDrawData(WaveformDrawData wfDrawData) {
		drawPackets.add(wfDrawData);
	}

	public void addPacket(SamplePacket packet, boolean isdemodulatedPacket) {
		origPackets.add(packet);
		// TODO if back from pause, remove old drawdata
		if (!StateHandler.isPaused()) {
			if (isdemodulatedPacket) {
				drawPackets.add(new WaveformDrawData(Arrays.copyOf(packet.getRe(), packet.getRe().length / 2),
						Arrays.copyOf(packet.getIm(), packet.getIm().length / 2), isdemodulatedPacket));
			} else {
				drawPackets
						.add(new WaveformDrawData(packet.getRe().clone(), packet.getIm().clone(), isdemodulatedPacket));
			}
		}
	}

	public WaveformDrawData[] getDrawData(int amount) {
		// TODO amount may be wrong (pixels)
		return drawPackets.getLast(amount);
		// SamplePacket[] usedPackets = new SamplePacket[5];
		//
		// float[] data = new float[amount];
		//
		// WaveformDrawData res = new WaveformDrawData();
	}

}
