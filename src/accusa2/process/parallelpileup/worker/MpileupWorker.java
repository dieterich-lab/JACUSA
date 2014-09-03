package accusa2.process.parallelpileup.worker;

import java.io.IOException;

import net.sf.samtools.SAMFileReader;
import accusa2.ACCUSA;
import accusa2.cli.parameters.SampleParameters;
import accusa2.cli.parameters.TwoSamplePileupParameters;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.iterator.AbstractParallelPileupWindowIterator;
import accusa2.pileup.iterator.TwoSampleIterator;
import accusa2.pileup.iterator.TwoSampleStrandedIterator;
import accusa2.pileup.iterator.TwoSampleUnstrandedIterator;
import accusa2.process.parallelpileup.dispatcher.pileup.MpileupWorkerDispatcher;
import accusa2.util.AnnotatedCoordinate;

public class MpileupWorker extends AbstractWorker {

	private final TwoSamplePileupParameters parameters;
	private final SAMFileReader[] readersA;
	private final SAMFileReader[] readersB;

	public MpileupWorker(MpileupWorkerDispatcher workerDispatcher, TwoSamplePileupParameters parameters) {
		super(workerDispatcher, parameters.getMaxThreads(), parameters.getOutput(), parameters.getFormat());
		this.parameters = parameters;

		readersA = initReaders(parameters.getSampleA().getPathnames());
		readersB = initReaders(parameters.getSampleB().getPathnames());
	}

	@Override
	protected void processParallelPileupIterator(AbstractParallelPileupWindowIterator parallelPileupIterator) {
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
	protected TwoSampleIterator buildParallelPileupIterator(AnnotatedCoordinate coordinate) {
		SampleParameters sampleA = parameters.getSampleA();
		SampleParameters sampleB = parameters.getSampleB();

		if (sampleA.getPileupBuilderFactory().isDirected() || 
				sampleB.getPileupBuilderFactory().isDirected()) {
			return new TwoSampleStrandedIterator(coordinate, readersA, readersB, sampleA, sampleB, parameters);
		}
		
		return new TwoSampleUnstrandedIterator(coordinate, readersA, readersB, sampleA, sampleB, parameters);
	}

	@Override
	protected void close() {
		close(readersA);
		close(readersB);
	}

}