package tech.ailef.wikiwho.utils;

import java.util.HashMap;

import tech.ailef.wikiwho.data.OrganizationReport;
import tech.ailef.wikiwho.data.WikipediaDiff;

public class HTMLExporter {
	
	public static String getDiffHTML(WikipediaDiff diff) {
		HashMap<String, Object> model = new HashMap<String, Object>();
		model.put("diff", diff);
		return VelocityRenderer.render(model, "templates/WikipediaDiffTemplate.vtl");
	}

	public static String getReportHTML(OrganizationReport report) {
		HashMap<String, Object> model = new HashMap<String, Object>();
		model.put("report", report);
		return VelocityRenderer.render(model, "templates/OrganizationReportTemplate.vtl");
	}
	
}
