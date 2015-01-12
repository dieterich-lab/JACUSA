package jacusa.filter.storage;


import jacusa.cli.parameters.AbstractParameters;

import net.sf.samtools.CigarElement;
import net.sf.samtools.SAMRecord;

public class DistanceFilterStorage extends AbstractWindowFilterStorage {

	private int distance;

	/**
	 * 
	 * @param c
	 * @param distance
	 */
	public DistanceFilterStorage(char c, int distance, AbstractParameters parameters) {
		super(c, parameters);
		this.distance = distance;
	}
	
	@Override
	public void processRecord(int genomicWindowStart, SAMRecord record) {
		processCigar(genomicWindowStart, record);

		/* INFO read start and read end not considered
		AlignmentBlock alignmentBlock;
		int windowPosition;

		// process read start and end
		List<AlignmentBlock> alignmentBlocks = record.getAlignmentBlocks();

		// read start
		alignmentBlock = alignmentBlocks.get(0);
		windowPosition = alignmentBlock.getReferenceStart() - genomicWindowStart;
		parseRecord(windowPosition, distance, alignmentBlock.getReadStart() - 1, record);

		// read end
		alignmentBlock = alignmentBlocks.get(alignmentBlocks.size() - 1); // get last alignment
		windowPosition = alignmentBlock.getReferenceStart() + alignmentBlock.getLength() - genomicWindowStart;
		parseRecord(windowPosition - distance, distance, alignmentBlock.getReadStart() - 1 + alignmentBlock.getLength() - distance, record);
		*/
	}

	// process INDELs
	@Override
	protected void processInsertion(int windowPosition, int readPosition, int genomicPosition, CigarElement cigarElement, SAMRecord record) {
		parseRecord(windowPosition - distance, distance, readPosition - distance, record);
		parseRecord(windowPosition + cigarElement.getLength(), distance, readPosition + cigarElement.getLength(), record);
	}

	// process INDELs
	@Override
	protected void processDeletion(int windowPosition, int readPosition, int genomicPosition, CigarElement cigarElement, SAMRecord record) {
		parseRecord(windowPosition - distance, distance, readPosition - distance, record);
		parseRecord(windowPosition, distance, readPosition, record);
	}

	// process SpliceSites
	@Override
	protected void processSkipped(int windowPosition, int readPosition, int genomicPosition, CigarElement cigarElement, SAMRecord record) {
		parseRecord(windowPosition - distance, distance, readPosition - distance, record);
		parseRecord(windowPosition + cigarElement.getLength(), distance, readPosition, record);
	}

	/**
	 * 
	 * @return
	 */
	public int getDistance() {
		return distance;
	}

}