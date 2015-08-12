package jacusa.method.call.statistic.dirmult.initalpha;

public class WeirBayesAlphaInit extends CombinedAlphaInit {
	
	public WeirBayesAlphaInit() {
		super("WeirBayes", new WeirAlphaInit(), new BayesAlphaInit());
	}
		
}