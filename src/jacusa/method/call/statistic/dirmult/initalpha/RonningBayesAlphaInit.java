package jacusa.method.call.statistic.dirmult.initalpha;

public class RonningBayesAlphaInit extends CombinedAlphaInit {
	
	public RonningBayesAlphaInit() {
		super("RonningBayes", new RonningAlphaInit(), new BayesAlphaInit());
	}
		
}