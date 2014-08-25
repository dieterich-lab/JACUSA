package accusa2.filter.cache.distance;

import net.sf.samtools.CigarElement;
import net.sf.samtools.SAMRecord;
import accusa2.cli.Parameters;
import accusa2.filter.cache.AbstractPileupBuilderFilterCache;

public class DistancePileupBuilderCache extends AbstractPileupBuilderFilterCache {

	private int distance;

	/**
	 * 
	 * @param c
	 * @param distance
	 */
	public DistancePileupBuilderCache(char c, int distance, Parameters parameters) {
		super(c, parameters);
		this.distance = distance;
	}
	
	// process read start and end
	@Override
	protected void processAlignmetMatch(int windowPosition, int readPosition, int genomicPosition, CigarElement cigarElement, SAMRecord record) {
		// TODO
	}
	
	// process INDELs
	@Override
	protected void processInsertion(int windowPosition, int readPosition, int genomicPosition, CigarElement cigarElement, SAMRecord record) {
		fillCache(windowPosition - distance, distance, readPosition - distance, -1, record);
		fillCache(windowPosition - cigarElement.getLength(), distance, readPosition + cigarElement.getLength(), -1, record);
	}
	@Override
	protected void processDeletion(int windowPosition, int readPosition, int genomicPosition, CigarElement cigarElement, SAMRecord record) {
		fillCache(windowPosition - distance, distance, readPosition - distance, -1, record);
		fillCache(windowPosition - cigarElement.getLength(), distance, readPosition + cigarElement.getLength(), -1, record);
	}

	// process SpliceSites
	@Override
	protected void processSkipped(int windowPosition, int readPosition, int genomicPosition, CigarElement cigarElement, SAMRecord record) {
		fillCache(windowPosition - distance, distance, readPosition - distance, -1, record);
		fillCache(windowPosition - cigarElement.getLength(), distance, readPosition + cigarElement.getLength(), -1, record);
	}

	/**
	 * 
	 * @return
	 */
	public int getDistance() {
		return distance;
	}

}