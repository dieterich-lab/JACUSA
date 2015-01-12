package jacusa.filter.storage;

import jacusa.cli.parameters.AbstractParameters;
import net.sf.samtools.CigarElement;
import net.sf.samtools.SAMRecord;

public class HomopolymerFilterStorage extends AbstractWindowFilterStorage {

	private int length;

	/**
	 * 
	 * @param c
	 * @param distance
	 */
	public HomopolymerFilterStorage(char c, int length, AbstractParameters parameters) {
		super(c, parameters);

		this.length = length;
	}

	@Override
	public void processRecord(int genomicWindowStart, SAMRecord record) {
		processCigar(genomicWindowStart, record);
	}

	@Override
	protected void processAlignmetMatch(int windowPosition, int readPosition, int genomicPosition, CigarElement cigarElement, SAMRecord record) {
		byte base = record.getReadBases()[readPosition]; 
		int readPositionStart = readPosition;
		int windowPositionStart = windowPosition;

		for (int readI = 1; readI < cigarElement.getLength(); ++readI) {
			if (base != record.getReadBases()[readPosition + readI]) {
				// fill cache
				int coveredRead = readPosition + readI - readPositionStart; 
				if (coveredRead >= length) {
					parseRecord(windowPositionStart, coveredRead, readPositionStart, record);
					
					/* DEBUG
					System.err.println(record.getReadName());
					for (int i = 0; i < coveredRead; ++i) {
						System.err.print(genomicPosition + i + " -> " + record.getReadBases()[readPositionStart + i]);
						System.err.print("\n");
					}
					*/
				}

				// reset
				base = record.getReadBases()[readPosition + readI];
				readPositionStart = readPosition + readI;
	
				windowPositionStart = windowPosition + readI;
			}
		}
	}

	public int getLength() {
		return length;
	}

}