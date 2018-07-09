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
	public void processRecord(int genomicWindowStart, int[] byte2int, SAMRecord record) {
		int windowPosition;

		// process read start and end
		List<AlignmentBlock> alignmentBlocks = record.getAlignmentBlocks();

		// read start
		final AlignmentBlock firstAlignmentBlock = alignmentBlocks.get(0);
		windowPosition = windowCache.getWindowCoordinates().convert2WindowPosition(firstAlignmentBlock.getReferenceStart());
		final int upstreamD = Math.min(distance + 1, firstAlignmentBlock.getLength());
		addRegion(windowPosition, upstreamD, firstAlignmentBlock.getReadStart() - 1, byte2int, record);
	
		// read end
		final AlignmentBlock lastAlignmentBlock = alignmentBlocks.get(alignmentBlocks.size() - 1); // get last alignment
		final int downstreamD = Math.min(distance + 1, lastAlignmentBlock.getLength());
		windowPosition = windowCache.getWindowCoordinates().convert2WindowPosition(
				lastAlignmentBlock.getReferenceStart() + lastAlignmentBlock.getLength() - downstreamD);
		// note: alignmentBlock.getReadStart() is 1-indexed
		addRegion(windowPosition, downstreamD, lastAlignmentBlock.getReadStart()  + lastAlignmentBlock.getLength() - downstreamD - 1, byte2int, record);
	}

	// process IN
	@Override
	public void processInsertion(int windowPosition, int readPosition, int genomicPosition, int upstreamMatch, int downstreamMatch, CigarElement cigarElement, int[] byte2int, SAMRecord record) {
 		int upstreamD = Math.min(distance + 1, upstreamMatch);
		addRegion(windowPosition - upstreamD, upstreamD, readPosition - upstreamD, byte2int, record);

		int downStreamD = Math.min(distance + 1, downstreamMatch);
		addRegion(windowPosition, downStreamD, readPosition + cigarElement.getLength(), byte2int, record);
	}

	// process DELs
	@Override
	public void processDeletion(int windowPosition, int readPosition, int genomicPosition, int upstreamMatch, int downstreamMatch, CigarElement cigarElement, int[] byte2int, SAMRecord record) {
		int upstreamD = Math.min(distance + 1, upstreamMatch);
		addRegion(windowPosition - upstreamD, upstreamD, readPosition - upstreamD, byte2int, record);

		windowPosition = windowCache.getWindowCoordinates().convert2WindowPosition(genomicPosition + cigarElement.getLength());
		int downStreamD = Math.min(distance + 1, downstreamMatch);
		addRegion(windowPosition, downStreamD, readPosition, byte2int, record);
	}

	// process SpliceSites
	@Override
	public void processSkipped(int windowPosition, int readPosition, int genomicPosition, int upstreamMatch, int downstreamMatch, CigarElement cigarElement, int[] byte2int, SAMRecord record) {
		int upstreamD = Math.min(distance + 1, upstreamMatch);
		addRegion(windowPosition - upstreamD, upstreamD, readPosition - upstreamD, byte2int, record);
		
		int downStreamD = Math.min(distance + 1, downstreamMatch);
		addRegion(windowPosition + cigarElement.getLength(), downStreamD, readPosition, byte2int, record);
	}

	/**
	 * 
	 * @return
	 */
	public int getDistance() {
		return distance;
	}

}