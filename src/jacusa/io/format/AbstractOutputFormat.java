package jacusa.io.format;

import jacusa.result.Result;

public abstract class AbstractOutputFormat {

	private char c;
	private String desc;
	
	public AbstractOutputFormat(char c, String desc) {
		this.c = c;
		this.desc = desc;
	}

	public final char getC() {
		return c;
	}

	public final String getDesc() {
		return desc;
	}

	// Header is empty by default
	public String getHeader() {
		return null;
	}

	public abstract String convert2String(Result result);

}