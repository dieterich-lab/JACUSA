package jacusa.filter.storage;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.util.WindowCoordinates;
import net.sf.samtools.CigarElement;
import net.sf.samtools.SAMRecord;

public class HomopolymerFilterStorage extends AbstractWindowFilterStorage {

	
	private int minLength;

	private int baseI;
	private int readPositionStart;
	private int windowPositionStart;

	/**
	 * 
	 * @param c
	 * @param distance
	 */
	public HomopolymerFilterStorage(
			final char c, 
			final int length, 
			final WindowCoordinates windowCoordinates,
			final SampleParameters sampleParameters,
			final AbstractParameters parameters) {
		super(c, windowCoordinates, sampleParameters, parameters);

		this.minLength = length;
	}

	@Override
	public void processAlignmentBlock(int windowPosition, int readPosition, int genomicPosition, CigarElement cigarElement, SAMRecord record) {
		// init
		readPositionStart = readPosition;
		windowPositionStart = windowPosition;
		baseI = record.getReadBases()[readPositionStart];

		for (int i = 1; i < cigarElement.getLength(); ++i) {
			if (baseI != record.getReadBases()[readPosition + i]) {
				// fill cache
				final int coveredReadLength = readPosition + i - readPositionStart; 
				if (coveredReadLength >= minLength) {
					parseRecord(windowPositionStart, coveredReadLength, readPositionStart, record);
				}

				// reset
				readPositionStart = readPosition + i;
				windowPositionStart = windowPosition + i;
				baseI = record.getReadBases()[readPositionStart];
			}
		}
	}

	public int getLength() {
		return minLength;
	}

}