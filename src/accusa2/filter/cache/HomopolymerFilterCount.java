package accusa2.filter.cache;

import net.sf.samtools.CigarElement;
import net.sf.samtools.SAMRecord;
import accusa2.cli.parameters.Parameters;

public class HomopolymerFilterCount extends AbstractPileupBuilderFilterCount {

	private int length;
	private int minDistance;

	/**
	 * 
	 * @param c
	 * @param distance
	 */
	public HomopolymerFilterCount(char c, int length, int distance, Parameters parameters) {
		super(c, parameters);
		this.length = length;
		this.minDistance = distance;
	}

	@Override
	public void processRecord(int genomicWindowStart, SAMRecord record) {
		processCigar(genomicWindowStart, record);
	}
	
	@Override
	protected void processAlignmetMatch(int windowPosition, int readPosition, int genomicPosition, CigarElement cigarElement, SAMRecord record) {
		byte base = record.getReadBases()[0];
		int start = 0;
		for (int i = 1; i < cigarElement.getLength(); ++i) {
			if (base != record.getReadBases()[i]) {
				// fill cache 
				if (i - start >= length) {
					// TODO add minDistance
					fillCache(windowPosition, i - start, readPosition + i, record);
				}

				// reset
				base = record.getReadBases()[i];
				start = i;
			}
		}
	}

	public int getLength() {
		return length;
	}

	/**
	 * 
	 * @return
	 */
	public int getDistance() {
		return minDistance;
	}

}