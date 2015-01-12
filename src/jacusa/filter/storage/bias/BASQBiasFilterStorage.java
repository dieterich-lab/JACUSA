package jacusa.filter.storage.bias;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.filter.storage.AbstractFilterStorage;
import jacusa.pileup.BaseConfig;
import net.sf.samtools.CigarElement;
import net.sf.samtools.SAMRecord;

public class BASQBiasFilterStorage extends AbstractFilterStorage<BiasContainer> {

	private BaseConfig baseConfig;
	private int windowSize;
	private int maxBASQ;
	
	public BASQBiasFilterStorage(final char c, final int maxBASQ, final AbstractParameters parameters) {
		super(c, parameters.getWindowSize());
		baseConfig = parameters.getBaseConfig();		

		windowSize = parameters.getWindowSize();
		int baseLength = baseConfig.getBaseLength();
		this.maxBASQ = maxBASQ;

		setData(new BiasContainer(windowSize, baseLength, maxBASQ));
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
			int basqI = (int)record.getBaseQualities()[readPosition + i];
			basqI = Math.min(basqI, maxBASQ);
			data[windowPosition + i][baseI][basqI] += 1;
		}
	}
	
}