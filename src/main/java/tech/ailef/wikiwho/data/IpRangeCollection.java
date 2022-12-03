package tech.ailef.wikiwho.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import tech.ailef.wikiwho.storage.BasicJsonSerializer;

public class IpRangeCollection {
	private static final BasicJsonSerializer<IpRangeCollection> serializer =
			new BasicJsonSerializer<>(IpRangeCollection.class);
	
	public static class IpRange {
		private String startIp;
		
		private String endIp;

		private long startIpLong;
		
		private long endIpLong;
		
		public IpRange(String startIp, String endIp) {
			this.startIp = startIp.trim();
			this.endIp = endIp.trim();
			this.startIpLong = ipToLong(this.startIp);
			this.endIpLong = ipToLong(this.endIp);
		}

		public String getStartIp() {
			return startIp;
		}

		public String getEndIp() {
			return endIp;
		}
		
		public long getStartIpLong() {
			if (startIpLong == 0)
				this.startIpLong = ipToLong(this.startIp.trim());
			return startIpLong;
		}
		
		public long getEndIpLong() {
			if (endIpLong == 0)
				this.endIpLong = ipToLong(this.endIp.trim());
			return endIpLong;
		}
		
		public static IpRange fromWildcard(String ip) {
			String startIp = ip.replace("*", "0");
			String endIp = ip.replace("*", "255");
			return new IpRange(startIp, endIp);
		}

		@Override
		public String toString() {
			return "IpRange [startIp=" + startIp + ", endIp=" + endIp + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((endIp == null) ? 0 : endIp.hashCode());
			result = prime * result + ((startIp == null) ? 0 : startIp.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			IpRange other = (IpRange) obj;
			if (endIp == null) {
				if (other.endIp != null)
					return false;
			} else if (!endIp.equals(other.endIp))
				return false;
			if (startIp == null) {
				if (other.startIp != null)
					return false;
			} else if (!startIp.equals(other.startIp))
				return false;
			return true;
		}
		
	}
	/**
	 * For each organization (key in the map) we store a list of ip ranges that we
	 * know are associated with it
	 */
	private HashMap<String, Set<IpRange>> ipranges = new HashMap<>();
	
	public HashMap<String, Set<IpRange>> getIpranges() {
		return ipranges;
	}
	
	public void addIpRange(String organization, IpRange range) {
		ipranges.putIfAbsent(organization, new HashSet<>());
		ipranges.get(organization).add(range);
	}

	public void stats() {
		System.out.println(ipranges.keySet().size() + " orgs");
	}
	
	public static long ipToLong(String ipAddress) {
		String[] ipAddressInArray = ipAddress.split("\\.");

		long result = 0;
		for (int i = 0; i < ipAddressInArray.length; i++) {
			int power = 3 - i;
			int ip = Integer.parseInt(ipAddressInArray[i]);
			result += ip * Math.pow(256, power);
		}

		return result;
	}

	private boolean match(Long ip, IpRange range) {
		long ipLo = range.getStartIpLong();
		long ipHi = range.getEndIpLong();
		
		return ip >= ipLo && ip <= ipHi;
	}
	
	public String match(String ip) {
		// Skip IPv6 addresses
		if (ip.contains(":")) return null;
		
		long longIp = ipToLong(ip.trim());
		
		for (String k : ipranges.keySet()) {
			Set<IpRange> ranges = ipranges.get(k);
			
			for (IpRange range : ranges) {
				if (match(longIp, range))
					return k;
			}
		}
		return null;
	}
	
	public String toJson() {
		return serializer.serialize(this);
	}
	
	public static IpRangeCollection fromJson(String json) {
		return serializer.deserialize(json);
	}
	
	public static IpRangeCollection fromFile(Path path) throws IOException {
		String content = new String(Files.readAllBytes(path), "UTF-8");
		return fromJson(content);
	}
}
