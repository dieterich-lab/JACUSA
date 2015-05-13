package jacusa.pileup.worker;

import jacusa.cli.parameters.OneSampleCallParameters;
import jacusa.pileup.dispatcher.call.OneSampleCallWorkerDispatcher;
import jacusa.pileup.iterator.AbstractWindowIterator;
import jacusa.pileup.iterator.OneSampleIterator;
import jacusa.pileup.iterator.variant.Variant;
import jacusa.pileup.iterator.variant.VariantParallelPileup;
import jacusa.util.Coordinate;
import net.sf.samtools.SAMFileReader;

@Deprecated
public class OneSampleCallWorker extends AbstractCallWorker {

	private SAMFileReader[] readers1;
	private final OneSampleCallParameters parameters;
	
	private final Variant variant;

	public OneSampleCallWorker(
			final OneSampleCallWorkerDispatcher threadDispatcher,
			final int threadId,
			final OneSampleCallParameters parameters) {
		super(
				threadDispatcher,
				threadId,
				parameters.getStatisticParameters(),
				parameters
		);

		this.parameters = parameters;
		readers1 = initReaders(parameters.getSample1().getPathnames());

		variant = new VariantParallelPileup();
	}

	@Override
	protected AbstractWindowIterator buildIterator(final Coordinate coordinate) {
		if (parameters.getSample1().getPileupBuilderFactory().isDirected()) {
			return new OneSampleIterator(coordinate, variant, readers1, parameters.getSample1(), parameters);
		}

		return new OneSampleIterator(coordinate, variant, readers1, parameters.getSample1(), parameters);
	}

	@Override
	protected void close() {
		close(readers1);
	}

}