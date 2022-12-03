package tech.ailef.wikiwho.server;

import java.util.HashMap;
import java.util.Map;

import spark.Request;
import spark.Response;
import spark.Spark;
import tech.ailef.wikiwho.data.OrganizationReport;
import tech.ailef.wikiwho.storage.MongoDatabases;
import tech.ailef.wikiwho.utils.VelocityRenderer;

public class OrganizationEndpoint extends APIEndpoint {
	@Override
	public synchronized Object handleRequest(Request request, Response response, ResponseFormat format) {
		if (!request.requestMethod().equals("GET")) return "{}";

		String organizationId = request.params("id");
		OrganizationReport report = MongoDatabases.organizationReports.getItem(organizationId);
		
		boolean success = true;
		
		if (organizationId == null) success = false;
		if (report == null) success = false;
		
		if (format == ResponseFormat.JSON) {
			return APIResponse.toJson(success, report);
		} else {
			if (success) {
				Map<String, Object> model = new HashMap<String, Object>();
				model.put("report", report);
				return VelocityRenderer.render(model, "templates/OrganizationReportTemplate.vtl");
			} else {
				response.redirect("/");
				Spark.halt();
				return "{}";
			}
		}
		
	}
}
