package accusa2.process.parallelpileup.dispatcher;

import java.io.IOException;

import accusa2.cli.Parameters;
import accusa2.io.format.ResultFormat;
import accusa2.io.output.Output;
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
	protected void processTmpLine(final ResultFormat resultFormat, Output output, Output filtered, String line) throws IOException {
		final double p = resultFormat.extractValue(line);
		if(parameters.getDebug()) {
			if(!parameters.getStatisticCalculator().filter(p)) {
				output.write(line + "\t" + p);

			}
		} else {
			if(p < 0.0) {
				filtered.write(line + "\t" + p);
			} else if(!parameters.getStatisticCalculator().filter(p)) {
				output.write(line + "\t" + p);
			}
		}
	}
	
}