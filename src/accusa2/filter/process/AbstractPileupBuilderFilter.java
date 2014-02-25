package accusa2.filter.process;

import accusa2.pileup.builder.AbstractPileupBuilder;

public abstract class AbstractPileupBuilderFilter {
	
	private char c;

	public AbstractPileupBuilderFilter(char c) {
		this.c = c;
	}

	public abstract void process(AbstractPileupBuilder pileupBuilder);

	public final char getC() {
		return c;
	}

}
