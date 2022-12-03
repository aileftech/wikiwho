package tech.ailef.wikiwho.server;

import java.util.HashMap;
import java.util.Map;

import spark.Request;
import spark.Response;
import spark.Spark;
import tech.ailef.wikiwho.data.WikipediaPageReport;
import tech.ailef.wikiwho.storage.MongoDatabases;
import tech.ailef.wikiwho.utils.VelocityRenderer;

public class PageEndpoint extends APIEndpoint {
	@Override
	public synchronized Object handleRequest(Request request, Response response, ResponseFormat format) {
		if (!request.requestMethod().equals("GET")) return "{}";

		boolean success = true;

		String pageId = request.params("pageid");
		
		WikipediaPageReport pageReport = MongoDatabases.pages.getItem(pageId);
		
		if (pageReport == null) success = false;
		
		if (success) {
			if (format == ResponseFormat.HTML) {
				Map<String, Object> model = new HashMap<String, Object>();
				
				model.put("report", pageReport);
				return VelocityRenderer.render(model, "templates/WikipediaPageReport.vtl");
			} else
				return APIResponse.toJson(true, pageReport);
		} else {
			if (format == ResponseFormat.HTML) {
				response.redirect("/");
				Spark.halt();
				return "{}";
			} else
				return APIResponse.toJson(false, pageReport);
		}
	}
}
