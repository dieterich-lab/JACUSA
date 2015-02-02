package jacusa.filter.storage;

//import java.util.List;

import java.util.List;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.util.WindowCoordinates;

import net.sf.samtools.AlignmentBlock;
//import net.sf.samtools.AlignmentBlock;
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
		windowPosition = windowCache.getWindowCoordinates().convert2WindowPosition(alignmentBlock.getReferenceStart());
		addRegion(windowPosition, distance, alignmentBlock.getReadStart() - 1, record);

		// read end
		alignmentBlock = alignmentBlocks.get(alignmentBlocks.size() - 1); // get last alignment
		windowPosition = windowCache.getWindowCoordinates().convert2WindowPosition(alignmentBlock.getReferenceStart() + alignmentBlock.getLength() - 1);
		// note alignmentBlock.getReadStart() is 1-indexed
		addRegion(windowPosition - distance, distance, alignmentBlock.getReadStart() - 1 + alignmentBlock.getLength() - 1 - distance, record);
	}
	
	/*
	@Override
	public void processAlignmentMatch(int windowPosition, int readPosition, int genomicPosition, final CigarElement cigarElement, SAMRecord record, int baseI, int qual) {
		if (readPosition < distance || record.getReadLength() - readPosition <= distance) {
			addBaseUnique(windowPosition, baseI, qual, record);
		}
	}
	*/

	// process IN
	@Override
	public void processInsertion(int windowPosition, int readPosition, int genomicPosition, int upstreamMatch, int downstreamMatch, CigarElement cigarElement, SAMRecord record) {
		int upstreamD = Math.min(distance, upstreamMatch);
		addRegion(windowPosition - upstreamD, upstreamD, readPosition - upstreamD, record);
		
		int downStreamD = Math.min(distance, downstreamMatch);
		addRegion(windowPosition + cigarElement.getLength(), downStreamD, readPosition + cigarElement.getLength(), record);
	}

	// process DELs
	@Override
	public void processDeletion(int windowPosition, int readPosition, int genomicPosition, int upstreamMatch, int downstreamMatch, CigarElement cigarElement, SAMRecord record) {
		int upstreamD = Math.min(distance, upstreamMatch);
		addRegion(windowPosition - upstreamD, upstreamD, readPosition - upstreamD, record);
		
		int downStreamD = Math.min(distance, downstreamMatch);
		addRegion(windowPosition, downStreamD, readPosition, record);
	}

	// process SpliceSites
	@Override
	public void processSkipped(int windowPosition, int readPosition, int genomicPosition, int upstreamMatch, int downstreamMatch, CigarElement cigarElement, SAMRecord record) {
		// TODO test
		int upstreamD = Math.min(distance, upstreamMatch);
		addRegion(windowPosition - upstreamD, upstreamD, readPosition - upstreamD, record);
		
		// int genomicPosition2 = windowCache.getWindowCoordinates().getGenomicPosition(windowPosition);
		
		int downStreamD = Math.min(distance, downstreamMatch);
		addRegion(windowPosition + cigarElement.getLength(), downStreamD, readPosition, record);
	}

	/**
	 * 
	 * @return
	 */
	public int getDistance() {
		return distance;
	}

}