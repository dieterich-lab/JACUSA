package jacusa.filter.storage;

import jacusa.cli.parameters.AbstractParameters;

import jacusa.pileup.BaseConfig;
import jacusa.pileup.builder.WindowCache;
import jacusa.process.phred2prob.Phred2Prob;

import net.sf.samtools.SAMRecord;

public abstract class AbstractWindowFilterStorage extends AbstractFilterStorage<WindowCache> {

	protected BaseConfig baseConfig;

	public AbstractWindowFilterStorage(char c, AbstractParameters parameters) {
		super(c, parameters.getWindowSize());

		int windowSize = parameters.getWindowSize();
		int baseLength = parameters.getBaseConfig().getBaseLength();
		setData(new WindowCache(windowSize, baseLength));

		baseConfig = parameters.getBaseConfig();
	}

	protected void parseRecord(int windowPosition, int length, int readPosition, SAMRecord record) {
		int offset = 0;

		if (readPosition < 0) {
			offset += Math.abs(readPosition);
			
			windowPosition += offset;
			readPosition += offset;
			length -= offset;
		}

		if (windowPosition < 0) {
			offset += Math.abs(windowPosition);
			
			windowPosition += offset;
			readPosition += offset;
			length -= offset;
		}

		for (int i = 0; i < length && windowPosition + i < getContainer().getWindowSize() && readPosition + i < record.getReadLength(); ++i) {
			if (! getVisited()[windowPosition + i]) {
				int baseI = baseConfig.getBaseI(record.getReadBases()[readPosition + i]);	

				// corresponds to N -> ignore
				if (baseI < 0) {
					continue;
				}

				byte qual = record.getBaseQualities()[readPosition + i];
				// quick fix
				qual = (byte)Math.min(qual, Phred2Prob.MAX_Q - 1);
				
				getContainer().add(windowPosition + i, baseI, qual);
				getVisited()[windowPosition + i] = true;
			}
		}
	}

	@Override
	public void clearContainer() {
		getContainer().clear();		
	}

}