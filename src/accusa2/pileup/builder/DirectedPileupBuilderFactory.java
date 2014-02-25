package accusa2.pileup.builder;


import java.util.Set;

import net.sf.samtools.SAMFileReader;
import accusa2.cli.Parameters;
import accusa2.pileup.Pileup;
import accusa2.util.AnnotatedCoordinate;

public class DirectedPileupBuilderFactory implements PileupBuilderFactory {

	private final Set<Character> bases;

	public DirectedPileupBuilderFactory(final Set<Character> bases) {
		this.bases = bases;
	}

	@Override
	public AbstractPileupBuilder newInstance(final AnnotatedCoordinate coordinate, final SAMFileReader reader, final Parameters parameters) {
		AbstractPileupBuilder pileupBuilder;

		if(bases.size() < Pileup.BASES2.length) {
			pileupBuilder = new RestrictedDirectedPileupBuilder(coordinate, reader, parameters);
		} else {
			pileupBuilder = new DirectedPileupBuilder(coordinate, reader, parameters);
		}

		return pileupBuilder;
	}

	public boolean isDirected() {
		return true;
	}

}
