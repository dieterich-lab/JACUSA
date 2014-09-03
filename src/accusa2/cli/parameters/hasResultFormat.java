package accusa2.cli.parameters;

import accusa2.io.format.result.AbstractResultFormat;

public interface hasResultFormat {

	AbstractResultFormat getFormat();
	void setFormat(AbstractResultFormat format);

}