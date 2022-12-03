package tech.ailef.wikiwho.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import spark.Request;
import spark.Response;
import tech.ailef.wikiwho.data.WikipediaDiff;
import tech.ailef.wikiwho.data.WikipediaReport;
import tech.ailef.wikiwho.storage.MongoDatabases;
import tech.ailef.wikiwho.utils.Hashing;
import tech.ailef.wikiwho.utils.VelocityRenderer;

public class HomeEndpoint extends APIEndpoint {
	public static class HomeResponse {
		private WikipediaReport report;
		
		private List<WikipediaDiff> diffs;

		public HomeResponse(WikipediaReport report, List<WikipediaDiff> diffs) {
			this.report = report;
			this.diffs = diffs;
		}

		public WikipediaReport getReport() {
			return report;
		}

		public List<WikipediaDiff> getDiffs() {
			return diffs;
		}
		
	}
	
	@Override
	public synchronized Object handleRequest(Request request, Response response, ResponseFormat format) {
		if (!request.requestMethod().equals("GET")) return "{}";

		String lang = "en";

		WikipediaReport report = MongoDatabases.wikipedias.getItem(Hashing.md5Hash(lang));
		
		List<WikipediaDiff> diffs = MongoDatabases.diffs.findAll(new Document(), new Document("votes", -1), 0, 10);
		
		if (report != null) {
			report.cutOrganizations(50);
			report.cutPages(50);
		}
		
		if (format == ResponseFormat.JSON)
			return APIResponse.toJson(true, new HomeResponse(report, diffs));
		
		Map<String, Object> model = new HashMap<String, Object>();
		
		model.put("report", new HomeResponse(report, diffs));
		return VelocityRenderer.render(model, "templates/HomeTemplate.vtl");
	}
}
