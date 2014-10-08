package accusa2.filter.factory;

import accusa2.filter.cache.AbstractCountFilterCache;
import accusa2.filter.feature.HomozygousFilter;

public class HomozygousFilterFactory extends AbstractFilterFactory {

	private char sample;

	public HomozygousFilterFactory() {
		super('H', "Filter non-homozygous pileup/BAM (A or B). Default: none");
		sample = 'N';
	}

	@Override
	public HomozygousFilter getFilterInstance() {
		return new HomozygousFilter(getC(), sample);
	}

	@Override
	public AbstractCountFilterCache getFilterCountInstance() {
		return null;
	}
	
	@Override
	public void processCLI(String line) throws IllegalArgumentException {
		if(line.length() == 1) {
			throw new IllegalArgumentException("Invalid argument " + line);
		}

		String[] s = line.split(Character.toString(AbstractFilterFactory.SEP));
		char sample = s[1].charAt(0);
		if (sample == 'A' || sample == 'B') {
			setSample(sample);
			return;
		}
		throw new IllegalArgumentException("Invalid argument " + sample);
	}

	public final void setSample(char sample) {
		this.sample = sample;
	}

	public final int getSample() {
		return sample;
	}

}
