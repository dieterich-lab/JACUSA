package jacusa.method.call.statistic.dirmult.initalpha;

public class WeirMoMBayesAlphaInit extends CombinedAlphaInit {
	
	public WeirMoMBayesAlphaInit() {
		super("WeirBayes", new WeirMoMAlphaInit(), new BayesAlphaInit());
	}
		
}