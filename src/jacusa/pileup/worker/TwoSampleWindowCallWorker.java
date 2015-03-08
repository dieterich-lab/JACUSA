package jacusa.pileup.worker;

import java.io.IOException;

import jacusa.JACUSA;
import jacusa.cli.parameters.SampleParameters;
import jacusa.cli.parameters.TwoSampleCallParameters;
import jacusa.io.format.result.BEDWindowResultFormat;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.dispatcher.call.TwoSampleWindowCallWorkerDispatcher;
import jacusa.pileup.iterator.AbstractWindowIterator;
import jacusa.pileup.iterator.TwoSampleWindowIterator;
import jacusa.pileup.iterator.variant.Variant;
import jacusa.pileup.iterator.variant.VariantParallelPileup;
import jacusa.util.Coordinate;

import net.sf.samtools.SAMFileReader;

public class TwoSampleWindowCallWorker extends AbstractCallWorker {

	private SAMFileReader[] readers1;
	private SAMFileReader[] readers2;
	private TwoSampleCallParameters parameters;

	private final Variant variant;

	public TwoSampleWindowCallWorker(final TwoSampleWindowCallWorkerDispatcher workerDispatcher, final TwoSampleCallParameters parameters) {
		super(workerDispatcher, parameters.getStatisticParameters().getStatisticCalculator(), parameters.getFormat(), parameters);

		this.parameters = parameters;
		readers1 = initReaders(parameters.getSample1().getPathnames());
		readers2 = initReaders(parameters.getSample2().getPathnames());

		variant = new VariantParallelPileup();
		synchronized (workerDispatcher) {
			parallelPileupIterator  = buildIterator(workerDispatcher.next(this));
		}
	}

	@Override
	protected TwoSampleWindowIterator buildIterator(final Coordinate coordinate) {
		SampleParameters sample1 = parameters.getSample1();
		SampleParameters sample2 = parameters.getSample2();
		
		return new TwoSampleWindowIterator(coordinate, variant, readers1, readers2, sample1, sample2, parameters);
	}
	
	@Override
	protected void processParallelPileupIterator(final AbstractWindowIterator parallelPileupIterator) {
		// print informative log
		JACUSA.printLog("Started screening window: " + 
				parallelPileupIterator.getCoordinate().getSequenceName() + 
				":" + 
				parallelPileupIterator.getCoordinate().getStart() + 
				"-" + 
				parallelPileupIterator.getCoordinate().getEnd());

		// iterate over parallel pileups
		while (parallelPileupIterator.hasNext()) {
			final ParallelPileup parallelPileup = parallelPileupIterator.getParallelPileup(); 

			// calculate unfiltered value
			final double unfilteredValue = getStatisticCalculator().getStatistic(parallelPileup);

			final StringBuilder sb = new StringBuilder();

			// no filters
			sb.append(getResultFormat().convert2String(parallelPileup, unfilteredValue, Character.toString(BEDWindowResultFormat.EMPTY)));
			
			// considered comparisons
			comparisons++;

			try {
				// write output 
				tmpOutputWriter.write(sb.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	protected void close() {
		readers1.clone();
		readers2.clone();
	}

}