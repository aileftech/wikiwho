package tech.ailef.wikiwho.storage;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bson.conversions.Bson;

public interface AbstractStorage<T, I> {
	public T getItem(I id);
	
	public void insertItem(T item);
	
	public void upsertItem(T item);
	
	public Iterator<T> iterator();
	
	public Iterator<T> iterator(Bson projection);
	
	public Iterator<T> iterator(Bson query, Bson projection);
	
	public List<T> getItems(Set<I> ids);
}
