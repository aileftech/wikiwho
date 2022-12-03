package tech.ailef.wikiwho.data;

import com.google.gson.annotations.SerializedName;

import tech.ailef.wikiwho.storage.BasicJsonSerializer;
import tech.ailef.wikiwho.storage.JsonObjectSerializer;
import tech.ailef.wikiwho.storage.JsonStorable;

public class WikipediaDiffEdit implements JsonStorable<String> {
	private static final BasicJsonSerializer<WikipediaDiffEdit> serializer =
		new BasicJsonSerializer<>(WikipediaDiffEdit.class);

	@SerializedName("_id")
	private String id;
	
	@SerializedName("changed_text")
	private String changedText;
	
	private WikipediaEditType type;
	
	public WikipediaDiffEdit(String id, String changedText, WikipediaEditType type) {
		this.id = id;
		this.changedText = changedText;
		this.type = type;
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

	public String getChangedText() {
		return changedText;
	}

	public WikipediaEditType getType() {
		return type;
	}
	
	
	
}
