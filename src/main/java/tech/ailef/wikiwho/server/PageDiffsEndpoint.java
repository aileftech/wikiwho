package tech.ailef.wikiwho.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.mongodb.client.model.Filters;

import spark.Request;
import spark.Response;
import tech.ailef.wikiwho.data.WikipediaDiff;
import tech.ailef.wikiwho.data.WikipediaPage;
import tech.ailef.wikiwho.storage.MongoDatabases;
import tech.ailef.wikiwho.utils.VelocityRenderer;

public class PageDiffsEndpoint extends APIEndpoint {
	
	
	@Override
	public synchronized Object handleRequest(Request request, Response response, ResponseFormat format) {
		if (!request.requestMethod().equals("GET")) return "{}";

		String pageId = request.params("pageid");
		
		long count = MongoDatabases.diffs.count(Filters.eq("to_id", Integer.parseInt(pageId)));
		int maxPage = (int)Math.ceil(1.0 * count / DiffsEndpoint.PAGE_SIZE);
		
		String pageNumber = request.params("pagenumber");
		int page = 0;
		if (pageNumber != null) {
			page = Integer.parseInt(pageNumber);
		}
		
		int fromIndex = page * DiffsEndpoint.PAGE_SIZE;
		List<WikipediaDiff> diffs = MongoDatabases.diffs.findAll(Filters.eq("to_id", Integer.parseInt(pageId)), new Document("timestamp", 1), fromIndex, DiffsEndpoint.PAGE_SIZE);
		
		String title = null;
		if (diffs.size() > 0) {
			title = diffs.get(0).getToTitle();
		}
		
		WikipediaPage wikipediaPage = new WikipediaPage(title, Integer.parseInt(pageId));
		
		if (format == ResponseFormat.JSON) {
			return APIResponse.toJson(true, new DiffsResponse(diffs, wikipediaPage, null, null, new Pagination(page, maxPage, DiffsEndpoint.PAGE_SIZE)));
		} else {
			Map<String, Object> model = new HashMap<String, Object>();
			
			model.put("diffs", new DiffsResponse(diffs, wikipediaPage, null, null, new Pagination(page, maxPage, DiffsEndpoint.PAGE_SIZE)));
			return VelocityRenderer.render(model, "templates/DiffsTemplate.vtl");
		}
	}
}
