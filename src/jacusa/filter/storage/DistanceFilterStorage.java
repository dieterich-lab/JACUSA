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
		final int upstreamD = Math.min(this.distance + 1, alignmentBlock.getLength());
		addRegion(windowPosition, upstreamD, alignmentBlock.getReadStart() - 1, record);
	
		// read end
		final int downstreamD = Math.min(this.distance + 1, alignmentBlock.getLength());
		alignmentBlock = alignmentBlocks.get(alignmentBlocks.size() - 1); // get last alignment
		windowPosition = windowCache.getWindowCoordinates().convert2WindowPosition(alignmentBlock.getReferenceStart() + alignmentBlock.getLength() - 1 - distance);
		// note: alignmentBlock.getReadStart() is 1-indexed
		addRegion(windowPosition, downstreamD, alignmentBlock.getReadStart() - 1 + alignmentBlock.getLength() - downstreamD, record);
	}

	// process IN
	@Override
	public void processInsertion(int windowPosition, int readPosition, int genomicPosition, int upstreamMatch, int downstreamMatch, CigarElement cigarElement, SAMRecord record) {
 		int upstreamD = Math.min(distance + 1, upstreamMatch);
		addRegion(windowPosition - upstreamD, upstreamD, readPosition - upstreamD, record);

		int downStreamD = Math.min(distance + 1, downstreamMatch);
		addRegion(windowPosition, downStreamD, readPosition + cigarElement.getLength(), record);
	}

	// process DELs
	@Override
	public void processDeletion(int windowPosition, int readPosition, int genomicPosition, int upstreamMatch, int downstreamMatch, CigarElement cigarElement, SAMRecord record) {
		int upstreamD = Math.min(distance + 1, upstreamMatch);
		addRegion(windowPosition - upstreamD, upstreamD, readPosition - upstreamD, record);

		windowPosition = windowCache.getWindowCoordinates().convert2WindowPosition(genomicPosition + cigarElement.getLength());
		int downStreamD = Math.min(distance + 1, downstreamMatch);
		addRegion(windowPosition, downStreamD, readPosition, record);
	}

	// process SpliceSites
	@Override
	public void processSkipped(int windowPosition, int readPosition, int genomicPosition, int upstreamMatch, int downstreamMatch, CigarElement cigarElement, SAMRecord record) {
		int upstreamD = Math.min(distance + 1, upstreamMatch);
		addRegion(windowPosition - upstreamD, upstreamD, readPosition - upstreamD, record);
		
		int downStreamD = Math.min(distance + 1, downstreamMatch);
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