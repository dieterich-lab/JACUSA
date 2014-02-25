/**
 * 
 */
package accusa2.pileup.builder;

import java.util.HashSet;
import java.util.Set;

import net.sf.samtools.CigarElement;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;

import accusa2.cli.Parameters;
import accusa2.pileup.Pileup;
import accusa2.util.AnnotatedCoordinate;

/**
 * @author michael
 *
 */
public class RestrictedDirectedPileupBuilder extends DirectedPileupBuilder {

	private final Set<Character> forwardBases;
	private final Set<Character> reverseBases;
	
	public RestrictedDirectedPileupBuilder(final AnnotatedCoordinate coordinate, final SAMFileReader reader, final Parameters parameters) {
		super(coordinate, reader, parameters);
		forwardBases = parameters.getBases();
		reverseBases = new HashSet<Character>(forwardBases.size());
		for(final char b : forwardBases) {
			reverseBases.add(Pileup.BASES[Pileup.COMPLEMENT[Pileup.BASE2INT.get(b)]]);
		}
	}

	@Override
	protected void processAlignmetMatch(int readPosition, int genomicPosition, final CigarElement cigarElement, final SAMRecord record) {
		for(int i = 0; i < cigarElement.getLength(); ++i) {
			char base = (char)record.getReadBases()[readPosition];
			if(record.getBaseQualities()[readPosition] < parameters.getMinBASQ() || 
					!record.getReadNegativeStrandFlag() && reverseBases.contains(base) ||
					record.getReadNegativeStrandFlag() && forwardBases.contains(base)) {
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
