package tech.ailef.wikiwho.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import tech.ailef.wikiwho.storage.BasicJsonSerializer;
import tech.ailef.wikiwho.storage.JsonObjectSerializer;
import tech.ailef.wikiwho.storage.JsonStorable;
import tech.ailef.wikiwho.utils.Hashing;

public class WikipediaReport implements JsonStorable<String> {
	private static final BasicJsonSerializer<WikipediaReport> serializer =
		new BasicJsonSerializer<>(WikipediaReport.class);
	
	@SerializedName("_id")
	private String id;
	
	private String language;
	
	private List<WikipediaPageStats> pages;
	
	private List<OrganizationWithCount> organizations;
	
	private WikipediaStats stats;
	
	@SerializedName("edit_count")
	private int countEdits = 0;
	
	private List<TimestampedEditCount> months;
	
	private WikipediaReport() {
		pages = new ArrayList<>();
		months = new ArrayList<>();
		organizations = new ArrayList<>();
	}
	
	public static class Builder {
		private WikipediaReport report = new WikipediaReport();
		
		private Map<WikipediaPage, Integer> pageEditCounts = new HashMap<>();
		
		private Map<String, Integer> countsTimestamp = new HashMap<>();
		
		private Map<Organization, Integer> organizationsCount = new HashMap<>();
		
		private Map<String, Integer> countIps = new HashMap<>();
		
		private int maxTimestamp = 0;
		
		public Builder(String wikipediaLanguage) {
			report.language = wikipediaLanguage;
			report.id = Hashing.md5Hash(wikipediaLanguage);
		}
		
		public Builder addIp(String ip) {
			countIps.merge(ip, 1, Integer::sum);
			return this;
		}
		
		public Builder incrementEditCount() {
			report.countEdits++;
			return this;
		}
		
		public Builder incrementPageEdit(WikipediaPage id) {
			pageEditCounts.merge(id, 1, Integer::sum);
			return this;
		}
		
		public Builder incrementOrganization(Organization org) {
			organizationsCount.merge(org, 1, Integer::sum);
			return this;
		}
		
		public Builder incrementTimestamp(WikipediaDiff diff) {
			String[] parts = diff.getTimestamp().split("-");
			String year = parts[0];
			String month = parts[1];
			
			countsTimestamp.merge(month + "-" + year, 1, Integer::sum);
			return this;
		}
		
		public WikipediaReport build() {
			List<WikipediaPage> orderedPages = pageEditCounts.entrySet()
				.stream()
				.sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
				.map(e -> e.getKey())
				.collect(Collectors.toList());
			
			orderedPages.forEach(page -> {
				report.pages.add(new WikipediaPageStats(page, pageEditCounts.get(page)));
			});
			
			report.organizations = organizationsCount.entrySet()
					.stream()
					.sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
					.map(e -> new OrganizationWithCount(e.getKey(), e.getValue()))
					.collect(Collectors.toList());
			
			int index = -1, processed = 0;
			for (int y = 2000; y < 2020; y++) {
				for (int m = 1; m < 13; m++) {
					Integer count = countsTimestamp.getOrDefault(m + "-" + y, 0);
					if (index < 0 && count != 0) index = processed; // Save index of first non-zero element
					
					report.months.add(new TimestampedEditCount(m + "-" + y, count));
					processed++;
				}
			}
			
			if (index != -1)
				report.months = report.months.subList(index, report.months.size());
			
			report.stats = new WikipediaStats(report.organizations.size(), report.pages.size(), report.countEdits);
			
			return report;
		}
	}

	public List<OrganizationWithCount> getOrganizations() {
		return organizations;
	}

	public int getEditCount() {
		return countEdits;
	}

	public WikipediaStats getStats() {
		return stats;
	}
	
	public List<WikipediaPageStats> getPages() {
		return pages;
	}
	
	public List<WikipediaPageStats> getSortedPages() {
		return getSortedPages(Integer.MAX_VALUE);
	}
	
	public List<WikipediaPageStats> getSortedPages(int limit) {
		return pages
					.stream()
					.sorted((a, b) -> Integer.compare(b.getEditCount(), a.getEditCount()))
					.limit(limit)
					.collect(Collectors.toList());
	}
	
	public void cutOrganizations(int len) {
		organizations = organizations.subList(0, Math.min(len, organizations.size()));
	}
	
	public void cutPages(int len) {
		pages = pages.subList(0, Math.min(len, pages.size()));
	}
	
	public List<TimestampedEditCount> getMonths() {
		return months;
	}
	
	@Override
	public String toJson() {
		return serializer.serialize(this);
	}

	public String getJsonLabels() {
		return new Gson().toJson(months.stream().map(x -> x.getTime()).collect(Collectors.toList()));
	}
	
	public String getJsonData() {
		return new Gson().toJson(months.stream().map(x -> x.getCount()).collect(Collectors.toList()));
	}
	
	@Override
	public JsonObjectSerializer<?> getSerializer() {
		return serializer;
	}

	@Override
	public String getId() {
		return id;
	}
}
