package jacusa.method.call.statistic.dirmult.initalpha;


import java.util.Map;

public class AlphaInitFactory {

	private Map<String, AbstractAlphaInit> alphaInits;

	public AlphaInitFactory(final Map<String, AbstractAlphaInit> alphaInits) {
		this.alphaInits = alphaInits;
	}

	public AbstractAlphaInit processCLI(String line) {
		final String initAlphaClass = line.split(Character.toString(','))[0];

		if (! alphaInits.containsKey(initAlphaClass)) {
			throw new IllegalArgumentException("Unknown initAlpha: " + line);
		}

		AbstractAlphaInit alphaInit = alphaInits.get(initAlphaClass).newInstance(line);
		
		return alphaInit;
	}
	
}
