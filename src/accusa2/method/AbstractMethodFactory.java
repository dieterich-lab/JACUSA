package accusa2.method;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import accusa2.cli.Parameters;
import accusa2.cli.options.ACOption;
import accusa2.process.parallelpileup.dispatcher.AbstractParallelPileupWorkerDispatcher;
import accusa2.process.parallelpileup.worker.AbstractParallelPileupWorker;
import accusa2.util.AnnotatedCoordinate;


public abstract class AbstractMethodFactory {

	protected Parameters parameters;
	protected Set<ACOption> acOptions;

	private String name;
	private String desc;

	public abstract void initACOptions();
	public abstract AbstractParallelPileupWorkerDispatcher<? extends AbstractParallelPileupWorker> getInstance(List<AnnotatedCoordinate> coordinates, Parameters parameters); 

	public AbstractMethodFactory(String name, String desc) {
		this.name = name;
		this.desc = desc;

		acOptions = new HashSet<ACOption>();
	}

	public Set<ACOption> getACOptions() {
		return acOptions;
	}

	public final String getName() {
		return name;
	}

	public final String getDescription() {
		return desc;
	}

	public final Parameters getParameters() {
		return parameters;
	}

	public final void setParameters(Parameters parameters) {
		this.parameters = parameters;
	}

}
