package jacusa.filter.storage.bias;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.filter.storage.AbstractFilterStorage;
import jacusa.pileup.BaseConfig;
import net.sf.samtools.CigarElement;
import net.sf.samtools.SAMRecord;

public class AlignmentBiasFilterStorage extends AbstractFilterStorage<BiasContainer> {

	private BaseConfig baseConfig;
	private int targetDistance;
	private int windowSize;

	public AlignmentBiasFilterStorage(final char c, final int targetReadLength, final AbstractParameters parameters) {
		super(c, parameters.getWindowSize());
		baseConfig = parameters.getBaseConfig();		
		this.targetDistance = targetReadLength;

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

		int alignmentPosition = record.getAlignmentStart();
		for (; i < cigarElement.getLength() && windowPosition + i < windowSize; ++i) {
			int baseI = baseConfig.getBaseI(record.getReadBases()[readPosition + i]);	

			// corresponds to N -> ignore
			if (baseI < 0) {
				continue;
			}

			// TODO
			int distance = genomicPosition - alignmentPosition; 
			distance = Math.min(targetDistance, distance);
			data[windowPosition + i][baseI][distance] += 1;
		}
	}

}