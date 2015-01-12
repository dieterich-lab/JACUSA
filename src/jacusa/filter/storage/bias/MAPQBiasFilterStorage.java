package jacusa.filter.storage.bias;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.filter.storage.AbstractFilterStorage;
import jacusa.pileup.BaseConfig;
import jacusa.util.WindowCoordinates;
import net.sf.samtools.SAMRecord;

public class MAPQBiasFilterStorage extends AbstractFilterStorage<BaseCount> {

	private BaseConfig baseConfig;
	private int windowSize;
	private int maxMAPQ;
	
	private int[][][] data;
	
	public MAPQBiasFilterStorage(
			final char c, 
			final int maxMAPQ, 
			final WindowCoordinates windowCoordinates,
			final AbstractParameters parameters) {
		super(c);
		baseConfig = parameters.getBaseConfig();		

		windowSize = parameters.getWindowSize();
		final int baseLength = baseConfig.getBaseLength();
		this.maxMAPQ = maxMAPQ;

		setContainer(new BaseCount(windowSize, baseLength, maxMAPQ));
		data = getContainer().getData();
	}

	public void processAlignmentMatch(
			int windowPosition, 
			int readPosition, 
			int genomicPosition, 
			final SAMRecord record,
			final int baseI,
			final int qual) {
		int mapqI = Math.min(record.getMappingQuality(), maxMAPQ);
		data[windowPosition][baseI][mapqI] += 1;
	}

}