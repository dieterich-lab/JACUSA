package jacusa.filter.storage;

import java.util.Arrays;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;

import jacusa.phred2prob.Phred2Prob;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.builder.WindowCache;
import jacusa.util.WindowCoordinates;

import net.sf.samtools.SAMRecord;

public abstract class AbstractWindowFilterStorage extends AbstractFilterStorage<WindowCache> {

	// count indel, read start/end, splice site as only 1!!!
	// this ensure that a base-call will only be counted once...
	private boolean[] visited;
	private BaseConfig baseConfig;

	private int windowSize;
	private WindowCache windowCache;

	private SampleParameters sampleParameters;
	
	// container for current SAMrecord
	private SAMRecord record;

	public AbstractWindowFilterStorage(final char c, 
			final WindowCoordinates windowCoordinates, 
			final SampleParameters sampleParameters, 
			final AbstractParameters parameters) {
		super(c);

		windowSize = parameters.getWindowSize();
		visited = new boolean[windowSize];
		
		final int baseLength = parameters.getBaseConfig().getBaseLength();
		setContainer(new WindowCache(windowCoordinates, baseLength));
		windowCache = getContainer();
		
		this.sampleParameters = sampleParameters;
		baseConfig = parameters.getBaseConfig();
	}

	protected void parseRecord(int windowPosition, int length, int readPosition, SAMRecord record) {
		if (this.record != record) {
			this.record = record;
			Arrays.fill(visited, false);
		}

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

		for (int i = 0; i < length && windowPosition + i < windowSize && readPosition + i < record.getReadLength(); ++i) {
			if (! visited[windowPosition + i]) {
				int baseI = baseConfig.getBaseI(record.getReadBases()[readPosition + i]);	

				// corresponds to N -> ignore
				if (baseI < 0) {
					continue;
				}

				byte qual = record.getBaseQualities()[readPosition + i];
				// quick fix
				qual = (byte)Math.min(qual, Phred2Prob.MAX_Q - 1);

				if (qual >= sampleParameters.getMinBASQ()) {
					windowCache.add(windowPosition + i, baseI, qual);
					visited[windowPosition + i] = true;
				}
			}
		}
	}

	@Override
	public void clearContainer() {
		getContainer().clear();		
	}

}