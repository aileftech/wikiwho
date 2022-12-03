package tech.ailef.wikiwho.data;

import java.util.ArrayList;
import java.util.Collections;
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

public class OrganizationReport implements JsonStorable<String> {
	private static final BasicJsonSerializer<OrganizationReport> serializer =
		new BasicJsonSerializer<>(OrganizationReport.class);
	
	@SerializedName("_id")
	private String id;
	
	private List<IpWithCount> ips;
	
	private Organization organization;
	
	@SerializedName("count_edits")
	private int countEdits = 0;
	
	private List<WikipediaPageStats> pages;
	
	private List<TimestampedEditCount> months;
	
	@SerializedName("last_edit")
	private String lastEdit;
	
	@SerializedName("last_edit_id")
	private String lastEditId;
	
	private OrganizationReport() {
		pages = new ArrayList<>();
		months = new ArrayList<>();
		ips = new ArrayList<>();
	}
	
	public static class Builder {
		private OrganizationReport report = new OrganizationReport();
		
		private Map<WikipediaPage, Integer> pageEditCounts = new HashMap<>();
		
		private Map<String, Integer> countsTimestamp = new HashMap<>();
		
		private Map<String, Integer> countIps = new HashMap<>();
		
		private int maxTimestamp = 0;
		
		public Builder(Organization organization) {
			report.organization = organization;
			report.id = Hashing.md5Hash(organization.getName());
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
		
		public Builder incrementTimestamp(WikipediaDiff diff) {
			String[] parts = diff.getTimestamp().split("-");
			String year = parts[0];
			String month = parts[1];
			
			if (diff.getIntTimestamp() > maxTimestamp) {
				maxTimestamp = diff.getIntTimestamp();
				report.lastEdit = diff.getReadableDate();
				report.lastEditId = diff.getId();
			}
			
			countsTimestamp.merge(month + "-" + year, 1, Integer::sum);
			return this;
		}
		
		public OrganizationReport build() {
			List<WikipediaPage> orderedPages = pageEditCounts.entrySet()
				.stream()
				.sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
				.map(e -> e.getKey())
				.collect(Collectors.toList());
			
			orderedPages.forEach(page -> {
				report.pages.add(new WikipediaPageStats(page, pageEditCounts.get(page)));
			});
			
			int index = -1, processed = 0;
			for (int y = 2000; y < 2021; y++) {
				for (int m = 1; m < 13; m++) {
					String mth = m + "";

					if (m < 10) mth = "0" + mth;
					Integer count = countsTimestamp.getOrDefault(mth + "-" + y, 0);
					if (index < 0 && count != 0) index = processed; // Save index of first non-zero element
					
					report.months.add(new TimestampedEditCount(m + "-" + y, count));
					processed++;
				}
			}
			
			int k = report.months.size() - 1;
			for ( ; k >= 0; k--) {
				TimestampedEditCount count = report.months.get(k);
				if (count.getCount() != 0) break;
			}
			report.months = report.months.subList(0, k + 1);
			
			if (index != -1)
				report.months = report.months.subList(index, report.months.size());
			
			countIps.forEach((ip, count) -> {
				report.ips.add(new IpWithCount(ip, count));
			});
			
			Collections.sort(report.ips, (a, b) -> Integer.compare(b.getCount(), a.getCount()));
			
			return report;
		}
	}

	public String getLastEditId() {
		return lastEditId;
	}
	
	public List<IpWithCount> getIps() {
		return ips;
	}

	public Organization getOrganization() {
		return organization;
	}

	public int getEditCount() {
		return countEdits;
	}

	public List<WikipediaPageStats> getPages() {
		return pages;
	}
	
	public String getLastEdit() {
		return lastEdit;
	}
	
	public int getNumEditedPages() {
		return pages.size();
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
