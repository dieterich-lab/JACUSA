package jacusa.filter.storage.bias;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.filter.storage.AbstractFilterStorage;
import jacusa.pileup.BaseConfig;
import net.sf.samtools.CigarElement;
import net.sf.samtools.SAMRecord;

public class MAPQBiasFilterStorage extends AbstractFilterStorage<BiasContainer> {

	private BaseConfig baseConfig;
	private int windowSize;
	private int maxMAPQ;
	
	public MAPQBiasFilterStorage(final char c, final int maxMAPQ, final AbstractParameters parameters) {
		super(c, parameters.getWindowSize());
		baseConfig = parameters.getBaseConfig();		

		windowSize = parameters.getWindowSize();
		int baseLength = baseConfig.getBaseLength();
		this.maxMAPQ = maxMAPQ;

		setData(new BiasContainer(windowSize, baseLength, maxMAPQ));
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

		int mapqI = record.getMappingQuality();
		mapqI = Math.max(mapqI, maxMAPQ - 1);
		for (; i < cigarElement.getLength() && windowPosition + i < windowSize; ++i) {
			int baseI = baseConfig.getBaseI(record.getReadBases()[readPosition + i]);	

			// corresponds to N -> ignore
			if (baseI < 0) {
				continue;
			}
			data[windowPosition + i][baseI][mapqI] += 1;
		}
	}
	
}