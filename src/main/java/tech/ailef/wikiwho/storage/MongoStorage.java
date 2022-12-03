package tech.ailef.wikiwho.storage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;

public class MongoStorage<T extends JsonStorable<I>, I> {
	protected JsonObjectSerializer<T> serializer;

	private MongoDatabase db;
	
	protected MongoCollection<Document> collection;
	
	
	/**
	 * Options to set upsert
	 */
	private ReplaceOptions upsert = new ReplaceOptions();
	
	public MongoStorage(MongoManager manager, String databaseName, String collectionName, JsonObjectSerializer<T> serializer) {
		this.serializer = serializer;
		
		upsert.upsert(true);
		
		db = manager.getDatabase(databaseName);
		collection = db.getCollection(collectionName);
	}
	
	public long count() {
		return collection.countDocuments();
	}
	
	public List<T> textSearch(String text) {
		return textSearch(text, Integer.MAX_VALUE);
	}
	
	public List<T> textSearch(String text, int limit) {
		Iterator<T> it = iterator(new Document("$text", new Document("$search", text)), new Document());
		List<T> results = new ArrayList<>();
		
		int count = 0;
		while (it.hasNext() && count < limit) {
			results.add(it.next());
			count++;
		}
		
		return results;
	}
	
	public long count(Bson filter) {
		return collection.countDocuments(filter);
	}
	
	public T getItem(I id) {
		Document first = collection.find(Filters.eq("_id", id)).first();
		if (first == null) return null;
		else return serializer.deserialize(first.toJson());
	}
	
	public void insertItem(T item) {
		if (item == null) {
			throw new RuntimeException("WTF");
		}
		
		if (item.getId() == null)
			throw new IllegalArgumentException("Cannot insert item with null ID: " + item.toJson());
		collection.insertOne(Document.parse(serializer.serialize(item)));
	}

	public Iterator<T> iterator(Bson projection) {
		MongoCursor<Document> iterator = collection.find().projection(projection).noCursorTimeout(true).iterator();
		return new Iterator<T>() {
			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public T next() {
				Document next = iterator.next();
				return serializer.deserialize(next.toJson());
			}
		};
	}
	
	public Iterator<T> iterator(Bson query, Bson projection) {
		return iterator(query, projection, 0);
	}
	
	public Iterator<T> iterator(Bson query, Bson projection, int skip) {
		MongoCursor<Document> iterator = collection.find(query).skip(skip).projection(projection).iterator();
		return new Iterator<T>() {
			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public T next() {
				Document next = iterator.next();
				return serializer.deserialize(next.toJson());
			}
		};
	}

	
	public T findFirst(Bson query, Bson projection, Bson sort) {
		Document doc = collection.find(query).projection(projection).sort(sort).first();
		
		T result = null;
		if (doc != null) {
			result = serializer.deserialize(doc.toJson());
		}
		
		return result;
	}
	
	public Iterator<T> sortedIterator(Bson query, Bson projection, Bson sort) {
		return sortedIterator(query, projection, sort, 0);
	}
	
	public Iterator<T> sortedIterator(Bson query, Bson projection, Bson sort, int skip) {
		MongoCursor<Document> iterator = 
			collection.find(query).projection(projection)
					.sort(sort).skip(skip).iterator();
		
		return new Iterator<T>() {
			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public T next() {
				Document next = iterator.next();
				return serializer.deserialize(next.toJson());
			}
		};
	}
	
	public Iterator<T> iterator() {
		return iterator(new Document());
	}
	
	public Iterator<T> sortedIterator(Bson projection, Bson sort) {
		return sortedIterator(new Document(), projection, sort);
	}
	
	public List<T> findAll(Bson query, Bson sort, int skip, int limit) {
		List<T> results = new ArrayList<>();
		Iterator<T> iterator = sortedIterator(query, new Document(), sort, skip);
		
		int count = 0;
		while (iterator.hasNext() && count++ < limit) {
			T doc = iterator.next();
			
			results.add(serializer.deserialize(doc.toJson()));
		}
		
		return results;
	}
	
	public List<T> findAll(Bson query, int skip, int limit) {
		List<T> results = new ArrayList<>();
		Iterator<T> iterator = iterator(query, new Document(), skip);
		
		int count = 0;
		while (iterator.hasNext() && count++ < limit) {
			T doc = iterator.next();
			
			results.add(serializer.deserialize(doc.toJson()));
		}
		
		return results;
	}
	
	public List<T> findAll(Bson query, int limit, Bson projection) {
		List<T> results = new ArrayList<>();
		Iterator<T> iterator = iterator(query, projection);
		
		int count = 0;
		while (iterator.hasNext() && count++ < limit) {
			T doc = iterator.next();
			
			results.add(serializer.deserialize(doc.toJson()));
		}
		
		return results;
	}
	
	public List<T> findAll(Bson query, int limit) {
		List<T> results = new ArrayList<>();
		Iterator<T> iterator = iterator(query, new Document());
		
		int count = 0;
		while (iterator.hasNext() && count++ < limit) {
			T doc = iterator.next();
			
			results.add(serializer.deserialize(doc.toJson()));
		}
		
		return results;
	}

	
	public MongoCollection<Document> getMongoCollection() {
		return collection;
	}

	public void upsertItem(T item) {
		if (item.getId() == null)
			throw new IllegalArgumentException("Cannot insert item with null ID: " + item.toJson());
		collection.replaceOne(Filters.eq("_id", item.getId()), Document.parse(serializer.serialize(item)), upsert); 
	}

	public List<T> getItems(Set<I> ids) {
		Iterator<T> iterator = iterator(Filters.in("_id", ids), new Document());
		
		List<T> result = new ArrayList<>();
		
		while (iterator.hasNext()) {
			result.add(iterator.next());
		}
		
		return result;
	}
}
