package accusa2.io.format;

public abstract class Helper {

	public static String implode(char c, boolean[] a) {
		StringBuilder sb = new StringBuilder();
		if(a.length == 0) {
			return sb.toString();
		}

		sb.append(a[0] ? 1 : 0);
		for(int i = 1; i< a.length; ++i) {
			sb.append(c);
			sb.append(a[i] ? 1 : 0);
		}
		return sb.toString();
	}

	public static String implode(char c, int[] a) {
		StringBuilder sb = new StringBuilder();
		if(a.length == 0) {
			return sb.toString();
		}

		sb.append(a[0]);
		for(int i = 1; i< a.length; ++i) {
			sb.append(c);
			sb.append(a[i]);
		}
		return sb.toString();
	}
	
}
