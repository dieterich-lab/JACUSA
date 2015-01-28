package jacusa.filter.storage;

import java.util.List;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.util.WindowCoordinates;

import net.sf.samtools.AlignmentBlock;
import net.sf.samtools.CigarElement;
import net.sf.samtools.SAMRecord;

public class DistanceFilterStorage extends AbstractWindowFilterStorage {

	private int distance;
	/**
	 * 
	 * @param c
	 * @param distance
	 */
	public DistanceFilterStorage(final char c, 
			final int distance,
			final WindowCoordinates windowCoordinates,
			final SampleParameters sampleParameters,
			final AbstractParameters parameters) {
		super(c, windowCoordinates, sampleParameters, parameters);
		this.distance = distance;
	}

	@Override
	public void processRecord(int genomicWindowStart, SAMRecord record) {
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
		windowPosition = alignmentBlock.getReferenceStart() + alignmentBlock.getLength() - 1 - genomicWindowStart;
		parseRecord(windowPosition - distance, distance, alignmentBlock.getReadStart() - 1 + alignmentBlock.getLength() - distance, record);
	}

	// process IN
	@Override
	public void processInsertion(int windowPosition, int readPosition, int genomicPosition, CigarElement cigarElement, SAMRecord record) {
		parseRecord(windowPosition - distance, distance, readPosition - distance, record);
		parseRecord(windowPosition + cigarElement.getLength(), distance, readPosition + cigarElement.getLength(), record);
	}

	// process DELs
	@Override
	public void processDeletion(int windowPosition, int readPosition, int genomicPosition, CigarElement cigarElement, SAMRecord record) {
		parseRecord(windowPosition - distance, distance, readPosition - distance, record);
		parseRecord(windowPosition, distance, readPosition, record);
	}

	// process SpliceSites
	@Override
	public void processSkipped(int windowPosition, int readPosition, int genomicPosition, CigarElement cigarElement, SAMRecord record) {
		// TODO test
		parseRecord(windowPosition - distance, 2 * distance, readPosition - distance, record);
		parseRecord(windowPosition + cigarElement.getLength() - distance, 2 * distance, readPosition + cigarElement.getLength(), record);
	}

	/**
	 * 
	 * @return
	 */
	public int getDistance() {
		return distance;
	}

}