package jacusa.pileup.worker;

import java.util.Map;

import jacusa.cli.parameters.TwoSampleCallParameters;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.dispatcher.call.TwoSampleDebugCallWorkerDispatcher;
import jacusa.pileup.iterator.TwoSampleDebugIterator;
import jacusa.util.Coordinate;

public class TwoSampleDebugCallWorker extends AbstractCallWorker {

	private Map<String, ParallelPileup> coord2parallelPileup;
	private TwoSampleCallParameters parameters;

	public TwoSampleDebugCallWorker(final TwoSampleDebugCallWorkerDispatcher threadDispatcher, TwoSampleCallParameters parameters) {
		super(threadDispatcher, parameters.getStatisticParameters().getStatisticCalculator(), parameters.getFormat(), parameters);

		coord2parallelPileup = threadDispatcher.getCoord2parallelPileup();
		this.parameters = parameters;

		synchronized (workerDispatcher) {
			parallelPileupIterator  = buildIterator(workerDispatcher.next(this));
		}
	}
	
	@Override
	protected void close() {
	// nothing to be done
	}

	@Override
	protected TwoSampleDebugIterator buildIterator(Coordinate coordinate) {
		// FIXME reader
		coordinate.setStart(coordinate.getStart() - 1);
		return new TwoSampleDebugIterator(coord2parallelPileup.get(coordinate.toString()), parameters);
	}

}
