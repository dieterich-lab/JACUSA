package jacusa.filter.storage;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.util.WindowCoordinates;
import net.sf.samtools.SAMRecord;

public class HomopolymerFilterStorage extends AbstractWindowFilterStorage {

	
	private int minLength;

	// store
	private SAMRecord record;
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

	// TODO check
	@Override
	public void processAlignmentMatch(
			int windowPosition, 
			int readPosition, 
			int genomicPosition, 
			final SAMRecord record,
			final int baseI,
			final int qual) {

		if (this.record == null) {
			this.record = record;
			readPositionStart = readPosition;
			windowPositionStart = windowPosition;
			this.baseI = baseI;
		} else if (this.record != record) {

			// fill cache
			int coveredReadLength = record.getReadLength() - readPositionStart; 
			if (coveredReadLength >= minLength) {
				parseRecord(windowPositionStart, coveredReadLength, readPositionStart, record);
			}

			// reset
			this.record = record;
			readPositionStart = readPosition;
			windowPositionStart = windowPosition;
			this.baseI = baseI;
		} else if (this.baseI != baseI) {
			// fill cache
			int coveredReadLength = readPosition - readPositionStart; 
			if (coveredReadLength >= minLength) {
				parseRecord(windowPositionStart, coveredReadLength, readPositionStart, record);

				/* DEBUG
				System.err.println(record.getReadName());
				for (int i = 0; i < coveredRead; ++i) {
					System.err.print(genomicPosition + i + " -> " + record.getReadBases()[readPositionStart + i]);
					System.err.print("\n");
				}
				*/
			}

			// reset
			readPositionStart = readPosition;
			windowPositionStart = windowPosition;
			this.baseI = baseI;
		}
	}

	public int getLength() {
		return minLength;
	}

}