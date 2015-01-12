package jacusa.util;

public abstract class StringCollapse {

	public static String collapse(double[] values, String sep) {
		StringBuilder sb = new StringBuilder();
		sb.append(values[0]);
		for (int i = 1; i < values.length; ++i) {
			sb.append(sep);
			sb.append(values[i]);			
		}
		return sb.toString();
	}

}