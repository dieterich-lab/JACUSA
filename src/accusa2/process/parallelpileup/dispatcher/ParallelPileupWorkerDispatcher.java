package accusa2.process.parallelpileup.dispatcher;

import java.io.IOException;

import accusa2.io.Output;
import accusa2.io.format.output.AbstractOutputFormat;
import accusa2.process.parallelpileup.worker.ParallelPileupWorker;
import accusa2.util.CoordinateProvider;

// TODO clean messy generic stuff
public class ParallelPileupWorkerDispatcher extends AbstractParallelPileupWorkerDispatcher<ParallelPileupWorker> {
	
	public ParallelPileupWorkerDispatcher(CoordinateProvider coordinateProvider, Parameters parameters) {
		super(coordinateProvider, parameters);
	}

	@Override
	protected ParallelPileupWorker buildNextParallelPileupWorker() {
		return new ParallelPileupWorker(this, parameters);
	}

	@Override
	protected void processFinishedWorker(
			ParallelPileupWorker processParallelPileup) {
		// nothing to be done
	}
	
	@Override
	protected void processTmpLine(final AbstractOutputFormat outputFormat, Output output, Output filtered, String line) throws IOException {
		final double p = outputFormat.extractValue(line); // TODO
		if (parameters.getDebug()) {
			if (! parameters.getStatisticCalculator().filter(p)) {
				output.write(line + "\t" + p);

			}
		} else {
			if (p < 0.0) {
				filtered.write(line + "\t" + p);
			} else if (! parameters.getStatisticCalculator().filter(p)) {
				output.write(line + "\t" + p);
			}
		}
	}

}