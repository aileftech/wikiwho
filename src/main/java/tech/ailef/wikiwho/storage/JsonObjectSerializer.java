package tech.ailef.wikiwho.storage;

public interface JsonObjectSerializer<T> {
	public String serialize(T item);
	
	public T deserialize(String json);
}
