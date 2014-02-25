package accusa2.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import accusa2.pileup.ParallelPileup;

public class DefaultStatisticContainer implements StatisticContainer {

	private int factor			= 10;
	private int size			= 1000;

	/*
	 * V = FP
	 * R = TP + FP
	 */

	private ValueContainer V;
	private ValueContainer R;

	public DefaultStatisticContainer() {
		V 	= new ValueContainer(factor, size);
		R	= new ValueContainer(factor, size);
	}

	public void addNULL_Value(final double value, final ParallelPileup parallelPileup) {
		V.addValue(value);
	}

	public void addR_Value(final double value, final ParallelPileup parallelPileup) {
		R.addValue(value);
	}

	public synchronized void addContainer(DefaultStatisticContainer c) {
		V.addStatisticContainer(c.V);
		R.addStatisticContainer(c.R);
	}

	public final int getFactor() {
		return factor;
	}

	public final int getSize() {
		return size;
	}

	public ValueContainer getR() {
		return R;
	}

	public ValueContainer getV() {
		return V;
	}

	public double getFDR(double value, ParallelPileup parallelPileup) {
		double v = (double)V.getCumulativeCount(value);
		double r = (double)R.getCumulativeCount(value);

		return Math.min(1.0, v / r);
	}

	public void write(String pathname) {
		File file = new File(pathname);
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			StringBuilder sb = new StringBuilder();

			sb.append("i");
			sb.append("\t");
			sb.append("V");
			sb.append("\t");
			sb.append("R");
			sb.append("\n");
			bw.write(sb.toString());
			
			for(int i = 0; i < size; ++i) {
				sb = new StringBuilder();
				sb.append(i);
				sb.append("\t");
				sb.append(V.getCount(i));
				sb.append("\t");
				sb.append(R.getCount(i));
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

	public void addContainer(StatisticContainer c) throws Exception {
		if(c instanceof DefaultStatisticContainer) {
			DefaultStatisticContainer d = (DefaultStatisticContainer)c;
			addContainer(d);
		} else {
			throw new Exception("Error! Not supported!");
		}
	}
	
}
