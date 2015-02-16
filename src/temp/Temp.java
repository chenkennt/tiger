package temp;

public class Temp {
	private static int count;
	private int num;

	public String toString() {
		return "t" + num;
	}

	public Temp() {
		num = count++;
	}

	public static int getCount() {
		return count;
	}
}