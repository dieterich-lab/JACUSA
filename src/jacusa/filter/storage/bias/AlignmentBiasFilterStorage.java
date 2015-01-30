package jacusa.filter.storage.bias;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.filter.storage.AbstractFilterStorage;
import jacusa.pileup.BaseConfig;
import jacusa.util.WindowCoordinates;
import net.sf.samtools.CigarElement;
import net.sf.samtools.SAMRecord;

public class AlignmentBiasFilterStorage extends AbstractFilterStorage<BaseCount> {

	private BaseConfig baseConfig;
	private int maxDistance; // max distance between position alignment start and aligned position
	private int windowSize;

	private int[][][] data;
	
	public AlignmentBiasFilterStorage(final char c, 
			final int targetReadLength, 
			final WindowCoordinates windowCoordinates,
			final AbstractParameters parameters) {
		super(c);
		baseConfig = parameters.getBaseConfig();		
		this.maxDistance = targetReadLength;

		windowSize = parameters.getWindowSize();
		final int baseLength = baseConfig.getBaseLength();

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
			final CigarElement cigarElement,
			final SAMRecord record,
			final int baseI,
			final int qual) {
		int distance = genomicPosition - record.getAlignmentStart(); 
		distance = Math.min(maxDistance, distance);
		data[windowPosition][baseI][distance] += 1;
	}

}