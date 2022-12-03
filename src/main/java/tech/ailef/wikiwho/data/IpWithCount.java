package tech.ailef.wikiwho.data;

public class IpWithCount {
	private String ip;
	
	private int count;

	public IpWithCount(String ip, int count) {
		this.ip = ip;
		this.count = count;
	}

	public String getIp() {
		return ip;
	}

	public int getCount() {
		return count;
	}
	
}
