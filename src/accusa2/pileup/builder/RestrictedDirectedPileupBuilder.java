/**
 * 
 */
package accusa2.pileup.builder;

import net.sf.samtools.CigarElement;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;

import accusa2.cli.Parameters;
import accusa2.util.AnnotatedCoordinate;

/**
 * @author michael
 *
 */
public class RestrictedDirectedPileupBuilder extends DirectedPileupBuilder {

	public RestrictedDirectedPileupBuilder(final AnnotatedCoordinate coordinate, final SAMFileReader reader, final Parameters parameters) {
		super(coordinate, reader, parameters);
	}

	@Override
	protected void processAlignmetMatch(int readPosition, int genomicPosition, final CigarElement cigarElement, final SAMRecord record) {
		for(int i = 0; i < cigarElement.getLength(); ++i) {
			char base = (char)record.getReadBases()[readPosition];
			if(record.getBaseQualities()[readPosition] < parameters.getMinBASQ() || base == 'N' || 
					!record.getReadNegativeStrandFlag() && parameters.getBasesComplemented().contains(base) ||
					record.getReadNegativeStrandFlag() && parameters.getBases().contains(base)) {
			} else {
				cachePosition(readPosition, genomicPosition, cigarElement, indelsBuffer, skippedBuffer, record);
			}
			// iterate
			++readPosition;
			++genomicPosition;
		}
	}
}
