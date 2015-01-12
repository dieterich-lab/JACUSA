package jacusa.filter.storage.bias;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.filter.storage.AbstractFilterStorage;
import jacusa.pileup.BaseConfig;
import jacusa.util.WindowCoordinates;
import net.sf.samtools.SAMRecord;

public class BASQBiasFilterStorage extends AbstractFilterStorage<BaseCount> {

	private BaseConfig baseConfig;
	private int windowSize;
	private int maxBASQ;
	
	private int[][][] data;
	
	public BASQBiasFilterStorage(final char c, 
			final int maxBASQ, 
			final WindowCoordinates windowCoordinates,
			final AbstractParameters parameters) {
		super(c);
		baseConfig = parameters.getBaseConfig();		

		windowSize = parameters.getWindowSize();
		final int baseLength = baseConfig.getBaseLength();
		this.maxBASQ = maxBASQ;

		setContainer(new BaseCount(windowSize, baseLength, maxBASQ));
		data = getContainer().getData();
	}

	@Override
	public void processAlignmentMatch(
			int windowPosition, 
			int readPosition, 
			int genomicPosition, 
			final SAMRecord record,
			final int baseI,
			final int qual) {
		int basqI = Math.min(qual, maxBASQ);
		data[windowPosition][baseI][basqI] += 1;
	}
	
}