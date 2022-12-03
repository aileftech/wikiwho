package tech.ailef.wikiwho.storage;

/**
 * An object storable as JSON. The generic refers to the type of the ID of the object
 * 
 * @param <I> the type of the ID field that identifies this item
 */
public interface JsonStorable<I> {
	public String toJson();
	public JsonObjectSerializer<?> getSerializer();
	public String getId();
}
