package accusa2.process.parallelpileup.worker;

import java.io.IOException;

import net.sf.samtools.SAMFileReader;
import accusa2.ACCUSA;
import accusa2.cli.parameters.SampleParameters;
import accusa2.cli.parameters.TwoSamplePileupParameters;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.iterator.AbstractWindowIterator;
import accusa2.pileup.iterator.AbstractTwoSampleIterator;
import accusa2.pileup.iterator.TwoSampleStrandedIterator;
import accusa2.pileup.iterator.TwoSampleUnstrandedIterator;
import accusa2.pileup.iterator.variant.AllParallelPileup;
import accusa2.pileup.iterator.variant.Variant;
import accusa2.process.parallelpileup.dispatcher.pileup.MpileupWorkerDispatcher;
import accusa2.util.AnnotatedCoordinate;

public class MpileupWorker extends AbstractWorker {

	private final TwoSamplePileupParameters parameters;
	private final SAMFileReader[] readersA;
	private final SAMFileReader[] readersB;

	private final Variant variant;
	
	public MpileupWorker(MpileupWorkerDispatcher workerDispatcher, TwoSamplePileupParameters parameters) {
		super(workerDispatcher, parameters.getMaxThreads(), parameters.getOutput(), parameters.getFormat());
		this.parameters = parameters;

		readersA = initReaders(parameters.getSampleA().getPathnames());
		readersB = initReaders(parameters.getSampleB().getPathnames());
		
		variant = new AllParallelPileup();
		
		parallelPileupIterator  = buildIterator(workerDispatcher.next(this));
	}

	@Override
	protected void processParallelPileupIterator(AbstractWindowIterator parallelPileupIterator) {
		ACCUSA.printLog("Started screening contig " + 
				parallelPileupIterator.getAnnotatedCoordinate().getSequenceName() + 
				":" + 
				parallelPileupIterator.getAnnotatedCoordinate().getStart() + 
				"-" + 
				parallelPileupIterator.getAnnotatedCoordinate().getEnd());

		while (parallelPileupIterator.hasNext()) {
			// considered comparisons
			comparisons++;

			StringBuilder sb = new StringBuilder();
			ParallelPileup parallelPileup = parallelPileupIterator.next();
			sb.append(parameters.getFormat().convert2String(parallelPileup));
			try {
				tmpOutputWriter.write(sb.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected AbstractTwoSampleIterator buildIterator(AnnotatedCoordinate coordinate) {
		SampleParameters sampleA = parameters.getSampleA();
		SampleParameters sampleB = parameters.getSampleB();

		if (sampleA.getPileupBuilderFactory().isDirected() || 
				sampleB.getPileupBuilderFactory().isDirected()) {
			return new TwoSampleStrandedIterator(coordinate, variant, readersA, readersB, sampleA, sampleB, parameters);
		}
		
		return new TwoSampleUnstrandedIterator(coordinate, variant, readersA, readersB, sampleA, sampleB, parameters);
	}

	@Override
	protected void close() {
		close(readersA);
		close(readersB);
	}

}