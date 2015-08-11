package jacusa.util;

import jacusa.pileup.DefaultPileup;
import jacusa.pileup.Pileup;

import java.text.DecimalFormat;

public abstract class Util {

	public static String printAlpha(double a[]) {
		DecimalFormat df = new DecimalFormat("0.0000"); 
		StringBuilder sb = new StringBuilder();

		sb.append(df.format(a[0]));
		for (int i = 1; i < a.length; ++i) {
			sb.append("  ");
			sb.append(df.format(a[i]));
		}
		return sb.toString();
	}

	public static Pileup[] flat(Pileup[] pileups, int[] variantBaseIs, int commonBaseI) {
		Pileup[] ret = new Pileup[pileups.length];
		for (int i = 0; i < pileups.length; ++i) {
			ret[i] = new DefaultPileup(pileups[i]);

			for (int variantBaseI : variantBaseIs) {
				ret[i].getCounts().add(commonBaseI, variantBaseI, pileups[i].getCounts());
				ret[i].getCounts().substract(variantBaseI, variantBaseI, pileups[i].getCounts());
			}
			
		}
		return ret;
	}
	
}
