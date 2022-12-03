package tech.ailef.wikiwho.wikiapi;

import java.io.IOException;

import org.jsoup.Jsoup;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import tech.ailef.wikiwho.data.WikipediaDiff;

public class WikiDiffRequest {
	private static final String COMPARE_REVISION_REQ = 
		"https://en.wikipedia.org/w/api.php?action=compare&fromrev=REV_ID&torelative=prev&prop=diff|size|ids|title&format=json";
	
	private static final String GET_REV_REQ = 
		"https://en.wikipedia.org/w/api.php?action=query&prop=revisions&revids=REV_ID&format=json";
	
	private static final Gson gson = new Gson();
	
	public WikipediaDiff getDiff(long revId) throws IOException {
		String url = COMPARE_REVISION_REQ.replace("REV_ID", revId + "");
		String response = Jsoup.connect(url).ignoreContentType(true).execute().body();
		
		try {
			JsonObject responseObj = gson.fromJson(response, JsonObject.class);
			
			if (!responseObj.has("compare")) return null;
			
			JsonObject obj = responseObj.get("compare").getAsJsonObject();
			
			long fromRevId = obj.get("fromrevid").getAsLong();
			long toRevId = obj.get("torevid").getAsLong();
			int fromSize = obj.get("fromsize").getAsInt();
			int toSize = obj.get("tosize").getAsInt();
			int fromId = obj.get("fromid").getAsInt();
			int toId = obj.get("toid").getAsInt();
			
			String fromTitle = obj.get("fromtitle").getAsString();
			String toTitle = obj.get("totitle").getAsString();
			String diffHtml = obj.get("*").getAsString();
			
			url = GET_REV_REQ.replace("REV_ID", revId + "");
			response = Jsoup.connect(url).ignoreContentType(true).execute().body();
			JsonObject r = gson.fromJson(response, JsonObject.class);
			
			String timestamp = null;
			try {
				JsonObject pages = r.get("query").getAsJsonObject()
						.get("pages").getAsJsonObject();
				
				String key = pages.keySet().iterator().next();
				JsonObject rev = pages.get(key).getAsJsonObject().get("revisions").getAsJsonArray().get(0).getAsJsonObject();
				timestamp = rev.get("timestamp").getAsString();
			} catch (NullPointerException | IndexOutOfBoundsException e) {
				e.printStackTrace();
			}
			
			
			WikipediaDiff diff = 
				new WikipediaDiff(fromTitle, toTitle, fromRevId, toRevId, fromSize, toSize, diffHtml, null, null, fromId, toId, timestamp);
			return diff;
		} catch (NullPointerException e) {
			return null;
		} catch (JsonSyntaxException e) {
			return null;
		}
	}
	
}
