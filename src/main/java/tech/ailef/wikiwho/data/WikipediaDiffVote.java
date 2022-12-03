package tech.ailef.wikiwho.data;

import com.google.gson.annotations.SerializedName;

import tech.ailef.wikiwho.storage.BasicJsonSerializer;
import tech.ailef.wikiwho.storage.JsonObjectSerializer;
import tech.ailef.wikiwho.storage.JsonStorable;
import tech.ailef.wikiwho.utils.Hashing;

public class WikipediaDiffVote implements JsonStorable<String> {
	private static final BasicJsonSerializer<WikipediaDiffVote> serializer =
		new BasicJsonSerializer<WikipediaDiffVote>(WikipediaDiffVote.class);
	
	@SerializedName("_id")
	private String id;
	
	public WikipediaDiffVote(String diffId, String ip) {
		this.id = Hashing.md5Hash(diffId + ip);
	}
	
	@Override
	public String toJson() {
		return serializer.serialize(this);
	}

	@Override
	public JsonObjectSerializer<?> getSerializer() {
		return serializer;
	}

	@Override
	public String getId() {
		return id;
	}
	

	
	
}
