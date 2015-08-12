package jacusa.method.call.statistic.dirmult.initalpha;

/**
 * Combines Ronning and Bayes to come up with an initial estimate for alpha.
 * 
 * @author Michael Piechotta
 */
public class RonningBayesAlphaInit extends CombinedAlphaInit {
	
	public RonningBayesAlphaInit() {
		super("RonningBayes", new RonningAlphaInit(), new BayesAlphaInit());
	}
		
}