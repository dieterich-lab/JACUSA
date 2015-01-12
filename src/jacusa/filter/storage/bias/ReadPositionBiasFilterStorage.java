package jacusa.filter.storage.bias;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.filter.storage.AbstractFilterStorage;
import jacusa.pileup.BaseConfig;
import jacusa.util.WindowCoordinates;
import net.sf.samtools.SAMRecord;

public class ReadPositionBiasFilterStorage extends AbstractFilterStorage<BaseCount> {

	private BaseConfig baseConfig;
	private int targetReadLength;
	private int windowSize;

	private int[][][] data;
	
	public ReadPositionBiasFilterStorage(final char c, 
			final int targetReadLength, 
			final WindowCoordinates windowCoordinates, 
			final AbstractParameters parameters) {
		super(c);
		baseConfig = parameters.getBaseConfig();		
		this.targetReadLength = targetReadLength;

		windowSize = parameters.getWindowSize();
		int baseLength = baseConfig.getBases().length;

		setContainer(new BaseCount(windowSize, baseLength, targetReadLength));
		data = getContainer().getData();
	}

	public void clearContainer() {
		getContainer().clear();
	}

	@Override
	public void processAlignmentMatch(
			int windowPosition, 
			int readPosition, 
			int genomicPosition, 
			final SAMRecord record,
			final int baseI,
			final int qual) {
		int normalizedReadPosition = normReadPosition(readPosition, record);

		// bias at begin and end is investigated 
		normalizedReadPosition = Math.min(targetReadLength - normalizedReadPosition, normalizedReadPosition);
		data[windowPosition][baseI][normalizedReadPosition] += 1;
	}

	private int normReadPosition(final int readPosition, final SAMRecord record) {
		return (int)((targetReadLength - 1) * (double)readPosition / (double)record.getReadLength()); 
	}

	public int getTargetReadLength() {
		return targetReadLength;
	}

}