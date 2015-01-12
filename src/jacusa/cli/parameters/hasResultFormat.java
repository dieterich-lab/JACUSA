package jacusa.cli.parameters;

import jacusa.io.format.result.AbstractResultFormat;

public interface hasResultFormat {

	AbstractResultFormat getFormat();
	void setFormat(AbstractResultFormat format);

}