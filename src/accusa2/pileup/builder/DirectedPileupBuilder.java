package accusa2.pileup.builder;

import java.util.List;

import net.sf.samtools.CigarElement;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import accusa2.cache.Coordinate;
import accusa2.cli.Parameters;
import accusa2.pileup.DefaultPileup.STRAND;
import accusa2.util.AnnotatedCoordinate;

/**
 * @author michael
 *
 */
// TODO
public class DirectedPileupBuilder extends UndirectedPileupBuilder {

	protected DefaultWindowCache forwardWindowCache;
	protected DefaultWindowCache reverseWindowCache;
	protected STRAND strand;
	
	public DirectedPileupBuilder(final AnnotatedCoordinate annotatedCoordinate, final SAMFileReader reader, final Parameters parameters) {
		super(annotatedCoordinate, reader, parameters);
	}

	protected void clearPileupCache() {
		forwardWindowCache.clear();
		reverseWindowCache.clear();

		// always consider forward strand first
		strand = STRAND.FORWARD;
	}

	public int getCoverage(int windowPosition) {
		return forwardWindowCache.getCoverage(windowPosition) + reverseWindowCache.getCoverage(windowPosition);
	}

	protected int cachePosition(final int windowPosition, final int readPosition, final int genomicPosition, final CigarElement cigarElement, final List<Coordinate> indels, final List<Coordinate> skipped, final SAMRecord record) {
		//  || FIXME coverageCache[windowPosition] <= parameters.getMaxDepth()) 
		if(windowPosition >= 0 && (parameters.getMaxDepth() == -1)) {
			byte base = 0; // FIXME Pileup.BASE2INT.get((char)record.getReadBases()[readPosition]);
			final byte qual = record.getBaseQualities()[readPosition];

			// FIXME
			if(strand == STRAND.REVERSE) {
				base = 0; // FIXME Pileup.COMPLEMENT[base];
				reverseWindowCache.add(windowPosition, base, qual);
			} else {
				forwardWindowCache.add(windowPosition, base, qual);
			}
		}

		return windowPosition;
	}

}