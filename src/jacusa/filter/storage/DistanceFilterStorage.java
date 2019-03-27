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
		// process read start and end
		List<AlignmentBlock> alignmentBlocks = record.getAlignmentBlocks();

		// read start
		final AlignmentBlock firstAlignmentBlock = alignmentBlocks.get(0);
		final int upstreamD = Math.min(distance + 1, firstAlignmentBlock.getLength());
		addRegion(firstAlignmentBlock.getReferenceStart(), upstreamD, firstAlignmentBlock.getReadStart() - 1, byte2int, record);
	
		// read end
		final AlignmentBlock lastAlignmentBlock = alignmentBlocks.get(alignmentBlocks.size() - 1); // get last alignment
		final int downstreamD = Math.min(distance + 1, lastAlignmentBlock.getLength());
		// note: alignmentBlock.getReadStart() is 1-indexed
		addRegion(lastAlignmentBlock.getReferenceStart() + lastAlignmentBlock.getLength() - downstreamD, downstreamD, lastAlignmentBlock.getReadStart()  + lastAlignmentBlock.getLength() - downstreamD - 1, byte2int, record);
	}

	// process IN
	@Override
	public void processInsertion(int readPosition, int genomicPosition, int upstreamMatch, int downstreamMatch, CigarElement cigarElement, int[] byte2int, SAMRecord record) {
 		int upstreamD = Math.min(distance + 1, upstreamMatch);
		addRegion(genomicPosition - upstreamD, upstreamD, readPosition - upstreamD, byte2int, record);

		int downStreamD = Math.min(distance + 1, downstreamMatch);
		addRegion(genomicPosition, downStreamD, readPosition + cigarElement.getLength(), byte2int, record);
	}

	// process DELs
	@Override
	public void processDeletion(int readPosition, int genomicPosition, int upstreamMatch, int downstreamMatch, CigarElement cigarElement, int[] byte2int, SAMRecord record) {
		int upstreamD = Math.min(distance + 1, upstreamMatch);
		addRegion(genomicPosition - upstreamD, upstreamD, readPosition - upstreamD, byte2int, record);

		int downStreamD = Math.min(distance + 1, downstreamMatch);
		addRegion(genomicPosition + cigarElement.getLength(), downStreamD, readPosition, byte2int, record);
	}

	// process SpliceSites
	@Override
	public void processSkipped(int readPosition, int genomicPosition, int upstreamMatch, int downstreamMatch, CigarElement cigarElement, int[] byte2int, SAMRecord record) {
		int upstreamD = Math.min(distance + 1, upstreamMatch);
		addRegion(genomicPosition - upstreamD, upstreamD, readPosition - upstreamD, byte2int, record);
		
		int downStreamD = Math.min(distance + 1, downstreamMatch);
		addRegion(genomicPosition + cigarElement.getLength(), downStreamD, readPosition, byte2int, record);
	}

	/**
	 * 
	 * @return
	 */
	public int getDistance() {
		return distance;
	}

}