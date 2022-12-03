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

public class WikipediaPageReport implements JsonStorable<String> {
	private static final BasicJsonSerializer<WikipediaPageReport> serializer =
		new BasicJsonSerializer<>(WikipediaPageReport.class);
	
	@SerializedName("_id")
	private String id;
	
	private WikipediaPage page;
	
	private List<OrganizationWithCount> organizations;
	
	private List<IpWithCount> ips;
	
	@SerializedName("count_edits")
	private int countEdits = 0;
	
	private List<TimestampedEditCount> months;
	
	private WikipediaPageReport() {
		months = new ArrayList<>();
		organizations = new ArrayList<>();
		ips = new ArrayList<>();
	}
	
	@SerializedName("last_edit")
	private String lastEdit;
	
	public static class Builder {
		private WikipediaPageReport report = new WikipediaPageReport();
		
		private Map<String, Integer> countsTimestamp = new HashMap<>();
		
		private Map<Organization, Integer> organizationsCount = new HashMap<>();
		
		private Map<String, Integer> countIps = new HashMap<>();
		
		private int maxTimestamp = 0;
		
		public Builder(WikipediaPage page) {
			report.page = page;
			report.id = page.getId() + "";
		}
		
		public Builder addIp(String ip) {
			countIps.merge(ip, 1, Integer::sum);
			return this;
		}
		
		public Builder incrementEditCount() {
			report.countEdits++;
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
			
			if (diff.getIntTimestamp() > maxTimestamp) {
				maxTimestamp = diff.getIntTimestamp();
				report.lastEdit = diff.getReadableDate();
			}
			
			countsTimestamp.merge(month + "-" + year, 1, Integer::sum);
			return this;
		}
		
		public WikipediaPageReport build() {
			report.organizations = organizationsCount.entrySet()
					.stream()
					.sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
					.map(e -> new OrganizationWithCount(e.getKey(), e.getValue()))
					.collect(Collectors.toList());
			
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
			
			if (index != -1)
				report.months = report.months.subList(index, report.months.size());

			int k = report.months.size() - 1;
			for ( ; k >= 0; k--) {
				TimestampedEditCount count = report.months.get(k);
				if (count.getCount() != 0) break;
			}
			report.months = report.months.subList(0, k + 1);
			
			
			countIps.forEach((ip, count) -> {
				report.ips.add(new IpWithCount(ip, count));
			});
			
			Collections.sort(report.ips, (a, b) -> Integer.compare(b.getCount(), a.getCount()));
			
			return report;
		}
	}

	public WikipediaPage getPage() {
		return page;
	}
	
	public List<OrganizationWithCount> getOrganizations() {
		return organizations;
	}

	public int getEditCount() {
		return countEdits;
	}

	public List<TimestampedEditCount> getMonths() {
		return months;
	}
	
	public List<IpWithCount> getIps() {
		return ips;
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
	
	public String getLastEdit() {
		return lastEdit;
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
