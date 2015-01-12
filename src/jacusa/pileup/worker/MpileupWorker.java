package jacusa.pileup.worker;

import jacusa.JACUSA;
import jacusa.cli.parameters.SampleParameters;
import jacusa.cli.parameters.TwoSamplePileupParameters;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.dispatcher.pileup.MpileupWorkerDispatcher;
import jacusa.pileup.iterator.AbstractTwoSampleIterator;
import jacusa.pileup.iterator.AbstractWindowIterator;
import jacusa.pileup.iterator.TwoSampleIterator;
import jacusa.pileup.iterator.variant.AllParallelPileup;
import jacusa.pileup.iterator.variant.Variant;
import jacusa.util.Coordinate;

import java.io.IOException;

import net.sf.samtools.SAMFileReader;

public class MpileupWorker extends AbstractWorker {

	private final TwoSamplePileupParameters parameters;
	private final SAMFileReader[] readers1;
	private final SAMFileReader[] readers2;

	private final Variant variant;
	
	public MpileupWorker(MpileupWorkerDispatcher workerDispatcher, TwoSamplePileupParameters parameters) {
		super(workerDispatcher, parameters.getMaxThreads(), parameters.getOutput(), parameters.getFormat());
		this.parameters = parameters;

		readers1 = initReaders(parameters.getSample1().getPathnames());
		readers2 = initReaders(parameters.getSample2().getPathnames());
		
		variant = new AllParallelPileup();
		
		parallelPileupIterator  = buildIterator(workerDispatcher.next(this));
	}

	@Override
	protected void processParallelPileupIterator(AbstractWindowIterator parallelPileupIterator) {
		JACUSA.printLog("Started screening contig " + 
				parallelPileupIterator.getCoordinate().getSequenceName() + 
				":" + 
				parallelPileupIterator.getCoordinate().getStart() + 
				"-" + 
				parallelPileupIterator.getCoordinate().getEnd());

		while (parallelPileupIterator.hasNext()) {
			// considered comparisons
			comparisons++;

			StringBuilder sb = new StringBuilder();
			parallelPileupIterator.next();
			ParallelPileup parallelPileup = parallelPileupIterator.getParallelPileup();
			
			sb.append(parameters.getFormat().convert2String(parallelPileup));
			try {
				tmpOutputWriter.write(sb.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected AbstractTwoSampleIterator buildIterator(Coordinate coordinate) {
		SampleParameters sample1 = parameters.getSample1();
		SampleParameters sample2 = parameters.getSample2();

		if (sample1.getPileupBuilderFactory().isDirected() || 
				sample2.getPileupBuilderFactory().isDirected()) {
			return new TwoSampleIterator(coordinate, variant, readers1, readers2, sample1, sample2, parameters);
		}
		
		return new TwoSampleIterator(coordinate, variant, readers1, readers2, sample1, sample2, parameters);
	}

	@Override
	protected void close() {
		close(readers1);
		close(readers2);
	}

}