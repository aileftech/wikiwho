package tech.ailef.wikiwho.storage;

import com.google.gson.Gson;

public class BasicJsonSerializer<T> implements JsonObjectSerializer<T> {
	private static final Gson gson = new Gson();
	
	private final Class<T> klass;
	
	public BasicJsonSerializer(Class<T> typeParameterClass) {
        this.klass = typeParameterClass;
    }

	@Override
	public String serialize(T item) {
		return gson.toJson(item);
	}

	@Override
	public T deserialize(String json) {
		return gson.fromJson(json, klass);
	}

}
