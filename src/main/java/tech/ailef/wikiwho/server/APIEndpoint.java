package tech.ailef.wikiwho.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.Request;
import spark.Response;
import spark.Route;

public abstract class APIEndpoint implements Route {
	private static Logger logger = LoggerFactory.getLogger(APIEndpoint.class);
	
	@Override
	public Object handle(Request request, Response response) throws Exception {
		logger.info("{"  + request.ip() + "} " + request.url() + (request.queryString() != null ? "?" + request.queryString() : ""));
		
		String param = request.queryParams("format");
		
		ResponseFormat format = ResponseFormat.HTML;
		
		if ((param != null && param.equalsIgnoreCase("json")) || isJsonOnly()) {
			response.header("Content-Type", "application/json; charset=utf-8");
			format = ResponseFormat.JSON;
		}
		
		return handleRequest(request, response, format);
	}
	
	public abstract Object handleRequest(Request request, Response response, ResponseFormat format);
	
	public boolean isJsonOnly() {
		return false;
	}
}
