package tech.ailef.wikiwho.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;

import com.google.gson.annotations.SerializedName;

import spark.Request;
import spark.Response;
import tech.ailef.wikiwho.data.WikipediaDiff;
import tech.ailef.wikiwho.storage.LuceneBackedIndex;
import tech.ailef.wikiwho.storage.LuceneIndexFactory;
import tech.ailef.wikiwho.utils.VelocityRenderer;

public class FullTextSearchEndpoint extends APIEndpoint {
	private static final LuceneBackedIndex index = LuceneIndexFactory.getInstance("data/index");
	
	public static class SearchResponse {
		private List<WikipediaDiff> diffs;

		private String query;
		
		private Pagination pagination;
		
		@SerializedName("search_removed")
		private boolean searchRemoved;
		
		@SerializedName("search_added")
		private boolean searchAdded;

		@SerializedName("organization_id")
		private String organizationId;
		
		@SerializedName("organization_name")
		private String organizationName;
		
		@SerializedName("total_results")
		private int totalResults;
		
		public SearchResponse(List<WikipediaDiff> diffs, String query, Pagination pagination, boolean searchRemoved, boolean searchAdded,
				String orgId, String organizationName, int totalResults) {
			this.diffs = diffs;
			this.query = query;
			this.pagination = pagination;
			this.searchRemoved = searchRemoved;
			this.searchAdded = searchAdded;
			this.organizationId = orgId;
			this.organizationName = organizationName;
			this.totalResults = totalResults;
		}
		
		public String getHTMLEscapedQuery() {
			return StringEscapeUtils.escapeHtml(query);
		}

		public List<WikipediaDiff> getDiffs() {
			return diffs;
		}
		
		public Pagination getPagination() {
			return pagination;
		}
		
		public String getOrganizationId() {
			return organizationId;
		}
		
		public String getOrganizationName() {
			return organizationName;
		}
		
		public boolean isSearchAdded() {
			return searchAdded;
		}
		
		public boolean isSearchRemoved() {
			return searchRemoved;
		}
		
		public String getQuery() {
			return query;
		}
		
		public int getTotalResults() {
			return totalResults;
		}
	}
	
	@Override
	public synchronized Object handleRequest(Request request, Response response, ResponseFormat format) {
		if (!request.requestMethod().equals("GET")) return "{}";

		String query = request.queryParams("query");
		if (query == null) {
			if (format == ResponseFormat.JSON) {
				return APIResponse.toJson(false, null);
			}
		}
		
		String queryAdded = request.queryParamOrDefault("searchAdded", "false");
		String queryRemoved = request.queryParamOrDefault("searchRemoved", "false");
		
		Boolean searchAdded = queryAdded.equalsIgnoreCase("on") ? true : false;
		Boolean searchRemoved = queryRemoved.equalsIgnoreCase("on") ? true : false;
		
		String orgId = request.queryParamOrDefault("orgId", "");
		String organizationName = request.queryParamOrDefault("organization", "");
		
		int pageNumber = Integer.parseInt(request.queryParamOrDefault("page", "0"));
		int maxPage = 0;
		List<WikipediaDiff> results = new ArrayList<>();
		int totalResults = 0;
		if (query != null) {
			List<WikipediaDiff> diffs = index.retrieve(query, searchAdded, searchRemoved, 1000);
			results.addAll(diffs);

			Collections.sort(results, (a, b) -> {
				return Integer.compare(a.getIntTimestamp(), b.getIntTimestamp());
			});

			int fromIndex = pageNumber * DiffsEndpoint.PAGE_SIZE;
			int toIndex = Math.min((pageNumber + 1) * DiffsEndpoint.PAGE_SIZE, results.size());
			
			totalResults = results.size();
			
			maxPage = (int)(Math.ceil(1.0 * totalResults / DiffsEndpoint.PAGE_SIZE));
			
			if (fromIndex > toIndex)
				results = new ArrayList<>();
			else
				results = results.subList(fromIndex, toIndex);
		}

		SearchResponse searchResponse = 
			new SearchResponse(
				results, 
				query, 
				new Pagination(pageNumber, maxPage, DiffsEndpoint.PAGE_SIZE),
				searchRemoved,
				searchAdded,
				orgId.isEmpty() ? null : orgId,
				organizationName.isEmpty() ? null : organizationName,
				totalResults
			);
		
		if (format == ResponseFormat.JSON) {
			return APIResponse.toJson(true, searchResponse);
		}
		
		Map<String, Object> model = new HashMap<String, Object>();
		
		model.put("search", searchResponse);
		return VelocityRenderer.render(model, "templates/FullTextSearchTemplate.vtl");
		
	}
}
