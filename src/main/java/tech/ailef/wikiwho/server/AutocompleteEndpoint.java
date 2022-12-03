package tech.ailef.wikiwho.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.Document;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import spark.Request;
import spark.Response;
import tech.ailef.wikiwho.data.OrganizationReport;
import tech.ailef.wikiwho.data.WikipediaPageReport;
import tech.ailef.wikiwho.storage.MongoDatabases;

public class AutocompleteEndpoint extends APIEndpoint {
	private static final Gson gson = new Gson();
	
	@SuppressWarnings("unused")
	private static class AutocompleteResult {
		private String id;
		
		private String name;
		
		@SerializedName("edit_count")
		private int numEdits;

		public AutocompleteResult(String id, String name, int numEdits) {
			this.id = id;
			this.name = name;
			this.numEdits = numEdits;
		}

		public String getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public int getNumEdits() {
			return numEdits;
		}
	}
	
	private void sortResults(List<AutocompleteResult> list, String query) {
		Collections.sort(list, (a, b) -> {
			if (a.getName().toLowerCase().startsWith(query.toLowerCase()) && !b.getName().toLowerCase().startsWith(query.toLowerCase()))
				return -1;
			if (!a.getName().toLowerCase().startsWith(query.toLowerCase()) && b.getName().toLowerCase().startsWith(query.toLowerCase()))
				return 1;
			
			int lengthDiff = a.getName().length() - b.getName().length();
			if (lengthDiff != 0) return lengthDiff;
			
			return b.getNumEdits() - a.getNumEdits();
		});
	}
	
	@Override
	public synchronized Object handleRequest(Request request, Response response, ResponseFormat format) {
		if (!request.requestMethod().equals("GET")) return "{}";

		String query = request.queryParams("query");
		
		if (query == null) {
			return gson.toJson(new ArrayList<>());
		}
		
		query = query.toLowerCase();
		
		String type = request.queryParamOrDefault("type", "org");
		
		if (type.equals("org")) {
			List<OrganizationReport> organizations = 
				MongoDatabases.organizationReports.findAll(
					new Document("organization.normalized_name", new Document("$regex", ".*" + query + ".*")), 
					1000,
					new Document("_id", 1).append("count_edits", 1).append("organization", 1)
				);
			
			List<AutocompleteResult> org = 
				organizations.stream().map(o -> new AutocompleteResult(
					o.getOrganization().getId(), 
					o.getOrganization().getName(), 
					o.getEditCount())
				).collect(Collectors.toList());
			sortResults(org,  query);
			org = org.subList(0, Math.min(org.size(), 20));
			return new Gson().toJson(org);
		} else if (type.equals("page")) {
			List<WikipediaPageReport> pages = 
					MongoDatabases.pages.findAll(
						new Document("page.normalized_name", new Document("$regex", ".*" + query + ".*")),
						1000,
						new Document("_id", 1).append("count_edits", 1).append("page", 1)
					);
			
			List<AutocompleteResult> org = pages.stream().map(o -> new AutocompleteResult(o.getPage().getId() + "", o.getPage().getTitle(), o.getEditCount())).collect(Collectors.toList());
			sortResults(org,  query);
			org = org.subList(0, Math.min(org.size(), 20));
			return gson.toJson(org);
		} else {
			return gson.toJson(new ArrayList<>());
		}
	}
	
	@Override
	public boolean isJsonOnly() {
		return true;
	}
}
