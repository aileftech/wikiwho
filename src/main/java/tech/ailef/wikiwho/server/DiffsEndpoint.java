package tech.ailef.wikiwho.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import spark.Request;
import spark.Response;
import spark.Spark;
import tech.ailef.wikiwho.data.Organization;
import tech.ailef.wikiwho.data.OrganizationReport;
import tech.ailef.wikiwho.data.WikipediaDiff;
import tech.ailef.wikiwho.data.WikipediaPage;
import tech.ailef.wikiwho.storage.MongoDatabases;
import tech.ailef.wikiwho.utils.VelocityRenderer;

public class DiffsEndpoint extends APIEndpoint {
	public static final int PAGE_SIZE = 15;
	
	@Override
	public synchronized Object handleRequest(Request request, Response response, ResponseFormat format) {
		if (!request.requestMethod().equals("GET")) return "{}";

		boolean success = true;
		
		String organizationId = request.params("orgid");
		if (organizationId == null) success = false;
		
		OrganizationReport organizationReport = MongoDatabases.organizationReports.getItem(organizationId);
		if (organizationReport == null) success = false;
		
		
		
		List<Document> clauses = new ArrayList<>();
		clauses.add(new Document("organization.id", organizationId));
		
		String pageNumber = request.params("pagenumber");
		int page = 0;
		if (pageNumber != null) {
			page = Integer.parseInt(pageNumber);
		}
		
		String pageIdParam = request.queryParams("pageid");
		if (pageIdParam != null)
			clauses.add(new Document("to_id", Integer.parseInt(pageIdParam)));
		
		String date = request.queryParams("month");
		if (date != null) {
			clauses.add(new Document("month", date));
		}
		
		Document query = new Document("$and", clauses);
		
		int fromIndex = page * PAGE_SIZE;
		
		long total = MongoDatabases.diffs.count(query);
		
		List<WikipediaDiff> results = MongoDatabases.diffs.findAll(query, new Document("timestamp", 1), fromIndex, PAGE_SIZE);
		
		Collections.sort(results, (a, b) -> {
			return Integer.compare(a.getIntTimestamp(), b.getIntTimestamp());
		});
		
		int maxPage = (int)(Math.ceil(1.0 * total / PAGE_SIZE));		
		
		
		String pageTitle = null;
		if (results.size() > 0 && pageIdParam != null) {
			pageTitle = results.get(0).getToTitle();
		}
		
		Integer pageId = pageIdParam == null ? null : Integer.parseInt(pageIdParam);
		
		WikipediaPage finalPageId = pageId == null ? null : new WikipediaPage(pageTitle, pageId);
		
		if (format == ResponseFormat.JSON) {
			if (success) {
				Organization organization = organizationReport.getOrganization();
				return APIResponse.toJson(true, new DiffsResponse(results, finalPageId, organization, date, new Pagination(page, maxPage, PAGE_SIZE)));
			} else
				return APIResponse.toJson(false, null);
		}
		
		if (success) {
			Organization organization = organizationReport.getOrganization();
			Map<String, Object> model = new HashMap<String, Object>();
			
			model.put("diffs", new DiffsResponse(results, finalPageId, organization, date, new Pagination(page, maxPage, PAGE_SIZE)));
			return VelocityRenderer.render(model, "templates/DiffsTemplate.vtl");
		} else {
			response.redirect("/");
			Spark.halt();
			return "{}";
		}
	}
}
