package accusa2.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import accusa2.pileup.ParallelPileup;

public class DiscriminantStatisticContainer implements StatisticContainer {

	private int factor			= 10;
	private int size			= 2000;
	
	/*
	 * V = FP
	 * R = TP + FP
	 */

	private ValueContainer ho_he_V;
	private ValueContainer ho_he_R;

	private ValueContainer he_he_V;
	private ValueContainer he_he_R;

	public DiscriminantStatisticContainer() {
		ho_he_V 	= new ValueContainer(factor, size);
		ho_he_R		= new ValueContainer(factor, size);

		he_he_V 	= new ValueContainer(factor, size);
		he_he_R		= new ValueContainer(factor, size);
	}

	/* (non-Javadoc)
	 * @see accusa2.util.StatisticContainer#addNULL_Value(double, accusa2.pileup.ParallelPileup)
	 */
	@Override
	public void addNULL_Value(final double value, final ParallelPileup parallelPileup) {
		if(parallelPileup.getPooledPileupA().getAlleles().length > 1 && parallelPileup.getPooledPileupB().getAlleles().length > 1) {
			he_he_V.addValue(value);
		} else {
			ho_he_V.addValue(value);
		}
	}

	/* (non-Javadoc)
	 * @see accusa2.util.StatisticContainer#addR_Value(double, accusa2.pileup.ParallelPileup)
	 */
	@Override
	public void addR_Value(final double value, final ParallelPileup parallelPileup) {
		if(parallelPileup.getPooledPileupA().getAlleles().length > 1 && parallelPileup.getPooledPileupB().getAlleles().length > 1) { 
			he_he_R.addValue(value);
		} else {
			ho_he_R.addValue(value);
		}
	}

	public synchronized void addContainer(DiscriminantStatisticContainer c) {
		ho_he_V.addStatisticContainer(c.ho_he_V);
		ho_he_R.addStatisticContainer(c.ho_he_R);

		he_he_V.addStatisticContainer(c.he_he_V);
		he_he_R.addStatisticContainer(c.he_he_R);
	}

	// HACK !!!! specific class expected
	@Override
	public void addContainer(StatisticContainer c) {
		DiscriminantStatisticContainer d = (DiscriminantStatisticContainer)c;
		addContainer(d);
	}
	
	/* (non-Javadoc)
	 * @see accusa2.util.StatisticContainer#getFactor()
	 */
	@Override
	public final int getFactor() {
		return factor;
	}

	/* (non-Javadoc)
	 * @see accusa2.util.StatisticContainer#getSize()
	 */
	@Override
	public final int getSize() {
		return size;
	}

	public ValueContainer getHe_he_R() {
		return he_he_R;
	}

	public ValueContainer getHe_he_V() {
		return he_he_V;
	}

	public ValueContainer getHo_he_R() {
		return ho_he_R;
	}

	public ValueContainer getHo_he_V() {
		return ho_he_V;
	}

	/* (non-Javadoc)
	 * @see accusa2.util.StatisticContainer#getFDR(double, accusa2.pileup.ParallelPileup)
	 */
	@Override
	public double getFDR(double value, ParallelPileup parallelPileup) {
		double V = 0.0;
		double R = 0.0;

		if(parallelPileup.getPooledPileupA().getAlleles().length > 1 && parallelPileup.getPooledPileupB().getAlleles().length > 1) { 
			V	= (double)he_he_V.getCumulativeCount(value);
			R 	= (double)he_he_R.getCumulativeCount(value);
		} else {
			V	= (double)ho_he_V.getCumulativeCount(value);
			R 	= (double)ho_he_R.getCumulativeCount(value);						
		}

		return Math.max(Double.MIN_VALUE, Math.min(1.0, V / R));
	}

	/* (non-Javadoc)
	 * @see accusa2.util.StatisticContainer#write(java.lang.String)
	 */
	@Override
	public void write(String pathname) {
		File file = new File(pathname);
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			StringBuilder sb = new StringBuilder();

			sb.append("i");
			sb.append("\t");
			sb.append("ho_he_V");
			sb.append("\t");
			sb.append("ho_he_R");
			sb.append("\t");
			sb.append("he_he_V");
			sb.append("\t");
			sb.append("he_he_R");
			sb.append("\n");
			bw.write(sb.toString());
			
			for(int i = 0; i < size; ++i) {
				sb = new StringBuilder();
				sb.append(i);
				sb.append("\t");
				sb.append(ho_he_V.getCount(i));
				sb.append("\t");
				sb.append(ho_he_R.getCount(i));
				sb.append("\t");
				sb.append(he_he_V.getCount(i));
				sb.append("\t");
				sb.append(he_he_R.getCount(i));
				sb.append("\n");
				bw.write(sb.toString());
			}

			bw.close();
			bw = null;
			file = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
