package tech.ailef.wikiwho.utils;

import java.util.Map;

import spark.ModelAndView;
import spark.Request;
import spark.template.velocity.VelocityTemplateEngine;

public class VelocityRenderer {
	private static final VelocityTemplateEngine engine = new VelocityTemplateEngine();
	
	public static String render(Map<String, Object> model, String template) {
		ModelAndView modelAndView = engine.modelAndView(model, template);
		
		return engine.render(modelAndView);
	}
	
	public static String render(Request request, Map<String, Object> model, String template) {
		return render(model, template);
	}
}
