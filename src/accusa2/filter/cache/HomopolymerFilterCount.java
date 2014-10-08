package accusa2.filter.cache;

import accusa2.cli.parameters.AbstractParameters;
import net.sf.samtools.CigarElement;
import net.sf.samtools.SAMRecord;

public class HomopolymerFilterCount extends AbstractCountFilterCache {

	private int length;
	private int minDistance;

	/**
	 * 
	 * @param c
	 * @param distance
	 */
	public HomopolymerFilterCount(char c, int length, int distance, AbstractParameters parameters) {
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
					fillCache(windowPosition, i - start - minDistance, readPosition + i + 2 * minDistance, record);
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