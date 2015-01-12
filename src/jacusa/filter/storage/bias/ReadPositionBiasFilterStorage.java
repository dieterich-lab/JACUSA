package jacusa.filter.storage.bias;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.filter.storage.AbstractFilterStorage;
import jacusa.pileup.BaseConfig;
import net.sf.samtools.CigarElement;
import net.sf.samtools.SAMRecord;

public class ReadPositionBiasFilterStorage extends AbstractFilterStorage<BiasContainer> {

	private BaseConfig baseConfig;
	private int targetReadLength;
	private int windowSize;

	public ReadPositionBiasFilterStorage(final char c, final int targetReadLength, final AbstractParameters parameters) {
		super(c, parameters.getWindowSize());
		baseConfig = parameters.getBaseConfig();		
		this.targetReadLength = targetReadLength;

		windowSize = parameters.getWindowSize();
		int baseLength = baseConfig.getBases().length;

		setData(new BiasContainer(windowSize, baseLength, targetReadLength));
	}

	public void clearContainer() {
		getContainer().clear();
	}

	@Override
	public void processRecord(int genomicWindowStart, SAMRecord record) {
		processCigar(genomicWindowStart, record);
	}

	protected void processAlignmetMatch(int windowPosition, int readPosition, int genomicPosition, final CigarElement cigarElement, final SAMRecord record) {
		BiasContainer container = getContainer();
		int[][][] data = container.getData();

		
		int i = 0;
		if (windowPosition < 0) {
			i = Math.abs(windowPosition);
		}
		for (; i < cigarElement.getLength() && windowPosition + i < windowSize; ++i) {
			int baseI = baseConfig.getBaseI(record.getReadBases()[readPosition + i]);	

			// corresponds to N -> ignore
			if (baseI < 0) {
				continue;
			}

			int normalizedReadPosition = normReadPosition(readPosition + i, record);
			normalizedReadPosition = Math.min(targetReadLength - normalizedReadPosition, normalizedReadPosition);
			data[windowPosition + i][baseI][normalizedReadPosition] += 1;
		}
	}
	
	private int normReadPosition(final int readPosition, final SAMRecord record) {
		return (int)((targetReadLength - 1) * (double)readPosition / (double)record.getReadLength()); 
	}

	public int getTargetReadLength() {
		return targetReadLength;
	}
	
}