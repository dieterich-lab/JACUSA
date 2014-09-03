package accusa2.process.parallelpileup.worker;

import net.sf.samtools.SAMFileReader;
import accusa2.cli.parameters.SampleParameters;
import accusa2.cli.parameters.TwoSampleCallParameters;
import accusa2.pileup.iterator.AbstractParallelPileupWindowIterator;
import accusa2.pileup.iterator.TwoSampleStrandedIterator;
import accusa2.pileup.iterator.TwoSampleUnstrandedIterator;
import accusa2.process.parallelpileup.dispatcher.call.TwoSampleCallWorkerDispatcher;
import accusa2.util.AnnotatedCoordinate;

public class TwoSampleCallWorker extends AbstractCallWorker {

	private SAMFileReader[] readersA;
	private SAMFileReader[] readersB;
	private TwoSampleCallParameters parameters;
	
	public TwoSampleCallWorker(final TwoSampleCallWorkerDispatcher threadDispatcher, TwoSampleCallParameters parameters) {
		super(threadDispatcher, parameters.getStatisticParameters().getStatisticCalculator(), parameters.getFormat(), parameters);
		this.parameters = parameters;

		readersA = initReaders(parameters.getSampleA().getPathnames());
		readersB = initReaders(parameters.getSampleA().getPathnames());
	}

	@Override
	protected AbstractParallelPileupWindowIterator buildParallelPileupIterator(final AnnotatedCoordinate coordinate) {
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