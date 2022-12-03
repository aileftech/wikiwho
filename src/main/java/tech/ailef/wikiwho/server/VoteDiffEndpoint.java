package tech.ailef.wikiwho.server;

import java.util.List;

import com.google.gson.Gson;
import com.mongodb.client.model.Filters;

import spark.Request;
import spark.Response;
import tech.ailef.wikiwho.data.WikipediaDiff;
import tech.ailef.wikiwho.data.WikipediaDiffVote;
import tech.ailef.wikiwho.storage.MongoDatabases;
import tech.ailef.wikiwho.utils.Hashing;

public class VoteDiffEndpoint extends APIEndpoint {
	private static final Gson gson = new Gson();
	
	@Override
	public synchronized Object handleRequest(Request request, Response response, ResponseFormat format) {
		if (!request.requestMethod().equals("POST")) return "{}";

		String ip = request.ip();
		
		String diffId = request.params("id");

		List<WikipediaDiffVote> find = MongoDatabases.diffsVote.findAll(Filters.eq("_id", Hashing.md5Hash(diffId + ip)), 1);
		
		if (find.size() > 0) {
			return APIResponse.toJson(false, null);
		} else {
			MongoDatabases.diffsVote.upsertItem(new WikipediaDiffVote(diffId, ip));
			
			WikipediaDiff item = MongoDatabases.diffs.getItem(diffId);
			item.incrementVotes();
			MongoDatabases.diffs.upsertItem(item);
			
			return APIResponse.toJson(true, null);
		}
	}
	
	@Override
	public boolean isJsonOnly() {
		return true;
	}
}
