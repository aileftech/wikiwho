package tech.ailef.wikiwho.data;

public class TimestampedEditCount {
	private String time;
	
	private int count;

	public TimestampedEditCount(String time, int count) {
		this.time = time;
		this.count = count;
	}
	
	public String getTime() {
		return time;
	}

	public int getCount() {
		return count;
	}
}
