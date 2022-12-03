package tech.ailef.wikiwho.server;

import java.util.HashMap;
import java.util.Map;

import spark.Request;
import spark.Response;
import tech.ailef.wikiwho.data.WikipediaDiff;
import tech.ailef.wikiwho.storage.MongoDatabases;
import tech.ailef.wikiwho.utils.VelocityRenderer;

public class DiffEndpoint extends APIEndpoint {
	public static final int PAGE_SIZE = 10;
	
	@Override
	public synchronized Object handleRequest(Request request, Response response, ResponseFormat format) {
		if (!request.requestMethod().equals("GET")) return "{}";

		String diffId = request.params("id");
		
		WikipediaDiff diff = MongoDatabases.diffs.getItem(diffId);
		
		
		if (format == ResponseFormat.JSON) {
			return APIResponse.toJson(true, diff);
		} else {
			Map<String, Object> model = new HashMap<String, Object>();
			
			model.put("diff", diff);
			return VelocityRenderer.render(model, "templates/SingleDiffPage.vtl");
		}
	}
}
