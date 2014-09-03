package accusa2.method.call.statistic;

import umontreal.iro.lecuyer.probdistmulti.DirichletDist;

public class StatisticUtils {

	public static double getDensity(final DirichletDist dirichlet, final double[][] probs) {
		double density = 0.0;

		for(int i = 0; i < probs.length; ++i) {
			density += Math.log(Math.max(Double.MIN_VALUE, dirichlet.density(probs[i])));
		}

		return density;
	}

}