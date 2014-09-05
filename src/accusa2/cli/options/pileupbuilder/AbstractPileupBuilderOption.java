package accusa2.cli.options.pileupbuilder;

import accusa2.cli.options.AbstractACOption;
import accusa2.pileup.builder.DirectedPileupBuilderFactory;
import accusa2.pileup.builder.PileupBuilderFactory;
import accusa2.pileup.builder.UndirectedPileupBuilderFactory;

public abstract class AbstractPileupBuilderOption extends AbstractACOption {

	protected static final char STRAND_SPECIFIC 	= 'S';
	protected static final char STRAND_UNSPECIFIC 	= 'U';
	protected static final char SEP 				= ',';

	public AbstractPileupBuilderOption() {
		opt = "P";
		longOpt = "build-pileup";
	}

	protected PileupBuilderFactory buildPileupBuilderFactory(boolean isDirected) {
		if(isDirected) {
			return new DirectedPileupBuilderFactory();
		} else {
			return new UndirectedPileupBuilderFactory();
		}
	}
	
	protected boolean parse(char c) {
		switch(c) {
		case STRAND_SPECIFIC:
			return true;

		case STRAND_UNSPECIFIC:
			return false;
			
		default:
			throw new IllegalArgumentException("Unknown '" + c + "'! Possible values for " + longOpt.toUpperCase() + ": S,S or U,U or S,U or U,S");
		}
	}

}