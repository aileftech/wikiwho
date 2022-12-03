package tech.ailef.wikiwho.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.annotations.SerializedName;

public class WikipageReport {
	@SerializedName("title")
	private String pageTitle;
	
	private Set<String> ips;
	
	private Set<Organization> organizations;
	
	@SerializedName("edit_count")
	private int editCount = 0;
	
	private List<WikipediaDiff> diffs;

	@SerializedName("months")
	private Map<String, Integer> countsTimestamp;
	
	private WikipageReport() {
		ips = new HashSet<>();
		diffs = new ArrayList<>();
		countsTimestamp = new HashMap<>();
	}
	
	public static class Builder {
		private WikipageReport report = new WikipageReport();

		public Builder(String pageTitle) {
			report.pageTitle = pageTitle;
		}
		
		public Builder incrementTimestamp(String timestamp) {
			String[] parts = timestamp.split("-");
			String year = parts[0];
			String month = parts[1];
			
			report.countsTimestamp.merge(month + "-" + year, 1, Integer::sum);
			return this;
		}
		
		public Builder addDiff(WikipediaDiff diff) {
			report.diffs.add(diff);
			return this;
		}
		
		public Builder addOrganization(Organization org) {
			report.organizations.add(org);
			return this;
		}
		
		public Builder addIp(String ip) {
			report.ips.add(ip);
			return this;
		}
		
		
		public WikipageReport build() {
			return report;
		}
	}

	public Set<String> getIps() {
		return ips;
	}

	public Set<Organization> getOrganizations() {
		return organizations;
	}

	public int getEditCount() {
		return editCount;
	}

	public List<WikipediaDiff> getDiffs() {
		return diffs;
	}
	
	public Map<String, Integer> getCountsTimestamp() {
		return countsTimestamp;
	}
	
	public List<WikipediaDiff> getDiffsForPage(String page) {
		return diffs.stream().filter(diff -> diff.getFromTitle().equals(page)).collect(Collectors.toList());
	}

	public String getPageTitle() {
		return pageTitle;
	}
	
	public List<String> getSortedTimestamps() {
		return getSortedTimestamps(Integer.MAX_VALUE);
	}
	
	public List<String> getSortedTimestamps(int limit) {
		return countsTimestamp.entrySet()
					.stream()
					.sorted((a, b) -> a.getKey().compareTo(b.getKey()))
					.map(e -> e.getKey())
					.limit(limit)
					.collect(Collectors.toList());
	}
}
