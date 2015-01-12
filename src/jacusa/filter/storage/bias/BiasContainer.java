package jacusa.filter.storage.bias;

import java.util.Arrays;

public class BiasContainer {

	private int windowSize;
	private int baseLength;
	
	private int dataSize; 
	private int[][][] data;

	public BiasContainer(int windowSize, int baseLength, int dataSize) {
		this.windowSize = windowSize;
		this.baseLength = baseLength;
		this.dataSize = dataSize;

		data = new int[windowSize][baseLength][dataSize];
	}

	public void clear() {
		for (int windowI = 0; windowI < windowSize; windowI++) {
			for (int baseI = 0; baseI < baseLength; baseI++) {
				Arrays.fill(data[windowI][baseI], 0);
			}
		}
	}

	public int[][][] getData() {
		return data;
	}

	public int getWindowSize() {
		return windowSize;
	}

	public int getBaseLength() {
		return baseLength;
	}
	
	public int getDataSize() {
		return dataSize;
	}

}