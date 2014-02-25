package accusa2.process.parallelpileup.dispatcher;


import java.io.File;
import java.io.IOException;
import java.util.List;

import accusa2.cli.Parameters;
import accusa2.io.output.Output;
import accusa2.io.output.TmpOutputReader;
import accusa2.process.parallelpileup.worker.MpileupParallelPileupWorker;
import accusa2.util.AnnotatedCoordinate;

public class MpileupParallelPileupWorkerDispatcher extends AbstractParallelPileupWorkerDispatcher<MpileupParallelPileupWorker> {

	public MpileupParallelPileupWorkerDispatcher(List<AnnotatedCoordinate> coordinates, Parameters parameters) {
		super(coordinates, parameters);
	}

	@Override
	protected void processFinishedWorker(MpileupParallelPileupWorker processParallelPileup) {
		synchronized (comparisons) {
			comparisons += processParallelPileup.getComparisons();
		}

		synchronized (tmpOutputs) {
			for(int i : processParallelPileup.getTmpOutputWriters().keySet()) {
				tmpOutputs[i] = processParallelPileup.getTmpOutputWriters().get(i);
			}
		}
	}

	@Override
	protected MpileupParallelPileupWorker buildNextParallelPileupWorker() {
		return new MpileupParallelPileupWorker(this, next(), parameters);
	}

	@Override
	protected void writeOuptut() {
		final Output output = parameters.getOutput();
		for(final Output tmpOutput : tmpOutputs) {
			try {
				tmpOutput.close();

				final TmpOutputReader tmpOutputReader = new TmpOutputReader(tmpOutput.getInfo());
				String line = null;
				while((line = tmpOutputReader.readLine()) != null) {
					output.write(line);
				}
				tmpOutputReader.close();

				new File(tmpOutput.getInfo()).delete();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

}
