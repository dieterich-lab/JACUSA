package accusa2.filter.cache.distance;

import java.util.List;

import net.sf.samtools.AlignmentBlock;
import net.sf.samtools.CigarElement;
import net.sf.samtools.SAMRecord;
import accusa2.cli.Parameters;
import accusa2.filter.cache.AbstractPileupBuilderFilterCount;

public class DistanceFilterCount extends AbstractPileupBuilderFilterCount {

	private int distance;

	/**
	 * 
	 * @param c
	 * @param distance
	 */
	public DistanceFilterCount(char c, int distance, Parameters parameters) {
		super(c, parameters);
		this.distance = distance;
	}

	@Override
	public void processRecord(int genomicWindowStart, SAMRecord record) {
		processCigar(genomicWindowStart, record);

		AlignmentBlock alignmentBlock;
		int windowPosition;
		
		// process read start and end
		List<AlignmentBlock> alignmentBlocks = record.getAlignmentBlocks();
		
		// read start
		alignmentBlock = alignmentBlocks.get(0);
		windowPosition = alignmentBlock.getReferenceStart() - genomicWindowStart;
		fillCache(windowPosition - distance, distance, alignmentBlock.getReadStart() - distance, record);

		// read end
		alignmentBlock = alignmentBlocks.get(alignmentBlocks.size() - 1); // get last alignment
		int offset = alignmentBlock.getReferenceStart() + alignmentBlock.getLength() - genomicWindowStart; // TODO check
		if (offset > distance) {
			return;
		}

		windowPosition = offset;
		fillCache(windowPosition - distance, distance, alignmentBlock.getReadStart() + alignmentBlock.getLength() - distance, record);
	}

	// process INDELs
	@Override
	protected void processInsertion(int windowPosition, int readPosition, int genomicPosition, CigarElement cigarElement, SAMRecord record) {
		fillCache(windowPosition - distance, distance, readPosition - distance, record);
		fillCache(windowPosition - cigarElement.getLength(), distance, readPosition + cigarElement.getLength(), record);
	}
	@Override
	protected void processDeletion(int windowPosition, int readPosition, int genomicPosition, CigarElement cigarElement, SAMRecord record) {
		fillCache(windowPosition - distance, distance, readPosition - distance, record);
		fillCache(windowPosition - cigarElement.getLength(), distance, readPosition + cigarElement.getLength(), record);
	}

	// process SpliceSites
	@Override
	protected void processSkipped(int windowPosition, int readPosition, int genomicPosition, CigarElement cigarElement, SAMRecord record) {
		fillCache(windowPosition - distance, distance, readPosition - distance, record);
		fillCache(windowPosition - cigarElement.getLength(), distance, readPosition + cigarElement.getLength(), record);
	}

	/**
	 * 
	 * @return
	 */
	public int getDistance() {
		return distance;
	}

}