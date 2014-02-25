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
public class RestrictedUndirectedPileupBuilder extends UndirectedPileupBuilder {

	public RestrictedUndirectedPileupBuilder(final AnnotatedCoordinate coordinate, final SAMFileReader reader, final Parameters parameters) {
		super(coordinate, reader, parameters);
	}

	@Override
	protected void processAlignmetMatch(int readPosition, int genomicPosition, final CigarElement cigarElement, final SAMRecord record) {
		for(int i = 0; i < cigarElement.getLength(); ++i) {
			if(record.getBaseQualities()[readPosition] < parameters.getMinBASQ() || 
					!parameters.getBases().contains((char)record.getReadBases()[readPosition])) {
				// iterate
				++readPosition;
				++genomicPosition;
			} else {
				cachePosition(readPosition, genomicPosition, cigarElement, indelsBuffer, skippedBuffer, record);
	
				// iterate
				++readPosition;
				++genomicPosition;
			}
		}

	}

}
