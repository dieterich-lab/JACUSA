package jacusa.util;

import jacusa.pileup.ParallelPileup;

@Deprecated
public interface StatisticContainer {

	public abstract void addNULL_Value(double value,
			ParallelPileup parallelPileup);

	public abstract void addR_Value(double value, ParallelPileup parallelPileup);

	public abstract void addContainer(StatisticContainer c) throws Exception;

	public abstract int getFactor();

	public abstract int getSize();

	public abstract double getFDR(double value, ParallelPileup parallelPileup);

	public abstract void write(String pathname);

}